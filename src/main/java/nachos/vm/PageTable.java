package nachos.vm;

import nachos.machine.*;
import nachos.threads.Lock;
import nachos.userprog.UserProcess;

import java.util.*;

public class PageTable {

    Processor processor;
    int tlbSize;
    public int numPhyPages;
    public Hashtable<String, TranslationEntry> invertedPageTable;
    /*** Encode pid and vpn to get string ***/
    public Hashtable<String, byte[]> pages;
    public int pageFaultCount = 0;
    public static int pageSize;

    public PageTable(int numPhyPages) {
        this.numPhyPages = numPhyPages;
        invertedPageTable = new Hashtable<>(numPhyPages);
        pages = new Hashtable<>(numPhyPages);
        pageSize = Processor.pageSize;
        processor = Machine.processor();
        tlbSize = processor.getTLBSize();
    }

    public String getKey(int processID, int vpn) {
        return processID + "," + vpn;
    }

    public boolean containsPage(int processID, int vpn) {
        return invertedPageTable.containsKey(getKey(processID, vpn));
    }

    public void replacePage(int processID, TranslationEntry tlbEntry) {
        //synchronized(invertedPageTable){
        String key = getKey(processID, tlbEntry.vpn);
        //if (invertedPageTable.containsKey(key)){
        invertedPageTable.put(key, tlbEntry);
        pages.put(key, getPhysicalPages(tlbEntry.ppn));
        //}
        //}
    }

    public void saveTLB(int processID) {
        int tlbSize = Machine.processor().getTLBSize();
        for (int i = 0; i < tlbSize; i++) {
            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
            if (tlbEntry.valid)
                replacePage(processID, tlbEntry);
        }
    }

    public void restoreTLB(int processID) {
        int index = 0;
        for (String key : invertedPageTable.keySet()) {
            if (key.startsWith(processID + ",")) {
                TranslationEntry entry = invertedPageTable.get(key);
                entry.used = false;
                Machine.processor().writeTLBEntry(index, entry);
                setPhysicalPages(entry.ppn, pages.get(key));
                index++;
                if (index == Machine.processor().getTLBSize())
                    return;
            }
        }
    }


    public void setPhysicalPage(int processID, int vpn, int ppn) {
        int offset = ppn * pageSize;
        byte[] memory = Machine.processor().getMemory();
        System.arraycopy(pages.get(getKey(processID, vpn)), 0, memory, offset, pageSize);
    }

    public void loadPageInTLB(int processID, int tlbEntryNo, TranslationEntry tlbEntry) {
        processor.writeTLBEntry(tlbEntryNo, tlbEntry);
        //System.out.println("Write in tlb position: "+tlbEntryNo+" vpn: "+tlbEntry.vpn);
        setPhysicalPage(processID, tlbEntry.vpn, tlbEntry.ppn);
    }

    public TranslationEntry getPage(int processID, int vpn, UserProcess process,
                                    TranslationEntry tlbEntryToBeReplaced) {
        String key = getKey(processID, vpn);

        if (!invertedPageTable.containsKey(key))
            handlePageFault(processID, vpn, process, tlbEntryToBeReplaced);

        return invertedPageTable.get(key);
    }

    public void handlePageFault(int processID, int vpn, UserProcess process,
                                TranslationEntry tlbEntryToBeReplaced) {

        VMKernel.pageTable.sanityCheck2(processID, "before page fault");
        pageFaultCount++;

        TranslationEntry replacedEntry = null;
        int newPPN;
        String replacedEntryKey = "";
        if (invertedPageTable.size() >= numPhyPages) {
//            if (tlbEntryToBeReplaced.valid)
            replacedEntry = tlbEntryToBeReplaced;
//            else
//                replacedEntry = getPageKeyToBeReplace(processID);

            newPPN = replacedEntry.ppn;
            replacedEntryKey = getKey(processID, replacedEntry.vpn);

            if (replacedEntry.valid && !replacedEntry.readOnly && replacedEntry.dirty)
                VMKernel.swapFile.write(replacedEntryKey, replacedEntry.ppn);
            else {
                TranslationEntry replacedEntry2 = invertedPageTable.get(replacedEntryKey);
                if (replacedEntry2 != null && !replacedEntry2.readOnly && replacedEntry2.dirty)
                    VMKernel.swapFile.write(replacedEntryKey, replacedEntry2.ppn);
            }

        } else
            newPPN = invertedPageTable.size();


        TranslationEntry missingEntry = new TranslationEntry(
                vpn, newPPN, true, false, false, false);

        boolean found = VMKernel.swapFile.read(getKey(processID, vpn), newPPN);

        if (!found) {
            if (process.codeSectionPageCount > vpn) {
                for (int i = 0; i < process.coff.getNumSections(); i++) {
                    CoffSection section = process.coff.getSection(i);
                    if ((section.getFirstVPN() + section.getLength()) > vpn) {
                        //System.out.println("PPN: "+ppn);
                        section.loadPage(vpn - section.getFirstVPN(), newPPN);
                        missingEntry.readOnly = section.isReadOnly();
                        break;
                    }
                }
            } else {
                Arrays.fill(Machine.processor().getMemory(), newPPN * pageSize,
                        (newPPN + 1) * pageSize, (byte) 0);
            }
        }

        // System.out.println("*****Page loaded with processID: " + processID + " vpn: " +
        //       missingEntry.vpn + " , ppn: " + missingEntry.ppn);

        // synchronized (invertedPageTable){

        if (invertedPageTable.size() >= numPhyPages) {
            //System.out.println("Remove page from PageTable ppn: " + newPPN + " key: " + replacedEntryKey);
            invertedPageTable.remove(replacedEntryKey);
            pages.remove(replacedEntryKey);
        }

        //System.out.println("Add page from PageTable vpn: " + missingEntry.vpn+" ppn: "+missingEntry.ppn);
        invertedPageTable.put(getKey(processID, vpn), missingEntry);
        pages.put(getKey(processID, vpn), getPhysicalPages(missingEntry.ppn));
        //}
    }

    public static byte[] getPhysicalPages(int ppn) {
        int offset = ppn * pageSize;
        byte[] memory = Machine.processor().getMemory();
        byte[] bytes = new byte[pageSize];
        System.arraycopy(memory, offset, bytes, 0, pageSize);
        return bytes;
    }

    public static void setPhysicalPages(int ppn, byte[] bytes) {
        int offset = ppn * pageSize;
        byte[] memory = Machine.processor().getMemory();
        System.arraycopy(bytes, 0, memory, offset, pageSize);
    }

    public void sanityCheck(int processID) {
        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);

            if (tlbEntry.valid) {
                if (!invertedPageTable.containsKey(getKey(processID, tlbEntry.vpn)))
                    System.out.println("#########################Failed");
            }
        }
    }

    public void sanityCheck2(int processID, String msg) {
//        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
//            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
//
//            if (tlbEntry.valid) {
//                if (!pages.containsKey(getKey(processID, tlbEntry.vpn))) {
//                    System.out.println("########### Failed => " + msg);
//                    System.out.println(tlbEntry.vpn);
//                } else if (!tlbEntry.dirty) {
//                    byte[] bytes = getPhysicalPages(tlbEntry.ppn);
//                    if (!Arrays.equals(bytes, pages.get(getKey(processID, tlbEntry.vpn)))) {
//                        System.out.println("########## Mem Failed => " + msg);
//                    }
//                }
//            }
//        }
    }

    public TranslationEntry getPageKeyToBeReplace(int processID) {
        TranslationEntry entry;
        Random random = new Random();
        Vector<String> notUsedNotDirtyValues = new Vector<>(numPhyPages);
        Vector<String> notUsedValues = new Vector<>(numPhyPages);
        HashSet<String> tlbEntryKeys = new HashSet<>();

        for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
            TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
            if (tlbEntry.valid)
                tlbEntryKeys.add(getKey(processID, tlbEntry.vpn));
        }

        Set<String> keySet = invertedPageTable.keySet();
        for (String key : keySet) {
            if (tlbEntryKeys.contains(key))
                continue;

            entry = invertedPageTable.get(key);
            if (!entry.dirty && !entry.used)
                notUsedNotDirtyValues.add(key);
            else if (!entry.used)
                notUsedValues.add(key);
        }

        String key = "";
        if (notUsedNotDirtyValues.size() != 0)
            key = notUsedNotDirtyValues.get(random.nextInt(notUsedNotDirtyValues.size()));
        if (notUsedValues.size() != 0)
            key = notUsedValues.get(random.nextInt(notUsedValues.size()));

        if (key.length() > 0)
            return invertedPageTable.get(key);
        for (String k : keySet) {
            if (!tlbEntryKeys.contains(k))
                invertedPageTable.get(key);
        }

        return invertedPageTable.get(keySet.toArray()[0]);
    }

}
