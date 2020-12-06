package nachos.vm;

import nachos.machine.*;
import nachos.threads.Lock;
import nachos.userprog.UserProcess;

import java.util.*;

public class PageTable {
    public int numPhyPages;
    /*** Encode pid and vpn to get string ***/
    public Hashtable<String, TranslationEntry> invertedPageTable;

    public PageTable(int numPhyPages) {
        this.numPhyPages = numPhyPages;
        invertedPageTable = new Hashtable<>(numPhyPages);
    }

    public String getKey(int processID, int vpn) {
        return processID + "," + vpn;
    }

    public boolean containsPage(int processID, int vpn) {
        return invertedPageTable.containsKey(getKey(processID, vpn));
    }

    public TranslationEntry getPage(int processID, int vpn) {
        return invertedPageTable.get(getKey(processID, vpn));
    }

    public void replacePage(int processID, TranslationEntry tlbEntry) {
        synchronized (invertedPageTable) {
            if (invertedPageTable.contains(processID + "," + tlbEntry.vpn))
                invertedPageTable.replace(processID + "," + tlbEntry.vpn, tlbEntry);
        }
    }

    public void handlePageFault(int processID, int vpn, UserProcess process) {
        int ppn; /// ppn => the physical page number where we load new page

        if (invertedPageTable.size() < numPhyPages)
            ppn = invertedPageTable.size();
        else {
            String key = getPageKeyToBeReplace();
            ppn = invertedPageTable.get(key).ppn;
            //if (tlbEntry.valid && !tlbEntry.readOnly && tlbEntry.dirty){}
            /************* (Mahathir & Abser)Save this page in swap space *************/
        }

        /********  (Mahathir & Abser) If page is in SwapSpace Take from swapspace ********/
        TranslationEntry translationEntry = null;

        //System.out.println(vmProcess.codeSectionPageCount);
        if (process.codeSectionPageCount > vpn) {
            for (int i = 0; i < process.coff.getNumSections(); i++) {
                CoffSection section = process.coff.getSection(i);
                if ((section.getFirstVPN() + section.getLength() - 1) < vpn)
                    continue;
                section.loadPage(vpn - section.getFirstVPN(), ppn);
                translationEntry = new TranslationEntry(
                        vpn, ppn, true, section.isReadOnly(), false, false);
                break;
            }
        } else {
            translationEntry = new TranslationEntry(vpn, ppn, true, false, false, false);
        }
        //System.out.println("*****Page loaded with processID: "+processID+" vpn: "+translationEntry.vpn+" , ppn: "+translationEntry.ppn);

        synchronized (invertedPageTable) {
            invertedPageTable.put(getKey(processID, vpn), translationEntry);
        }
    }

    public String getPageKeyToBeReplace() {
        TranslationEntry tlbEntry;
        Random random = new Random();
        Vector<String> invalidValues = new Vector<>(numPhyPages);
        Vector<String> notUsedNotDirtyValues = new Vector<>(numPhyPages);
        Vector<String> notUsedValues = new Vector<>(numPhyPages);

        Set<String> keySet = invertedPageTable.keySet();
        for (String key : keySet) {
            tlbEntry = invertedPageTable.get(key);
            if (!tlbEntry.valid)
                invalidValues.add(key);
            if (!tlbEntry.dirty && !tlbEntry.used)
                notUsedNotDirtyValues.add(key);
            else if (!tlbEntry.used)
                notUsedValues.add(key);
        }

        if (invalidValues.size() != 0)
            return invalidValues.get(random.nextInt(invalidValues.size()));
        if (notUsedNotDirtyValues.size() != 0)
            return notUsedNotDirtyValues.get(random.nextInt(notUsedNotDirtyValues.size()));
        if (notUsedValues.size() != 0)
            return notUsedValues.get(random.nextInt(notUsedValues.size()));

        int r = random.nextInt(numPhyPages);

        int i = 0;
        for (String key : keySet) {
            if (i == r)
                return key;
            i++;
        }
        return "";
    }

}
