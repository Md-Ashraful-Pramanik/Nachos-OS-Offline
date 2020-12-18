package nachos.vm;

import nachos.machine.Machine;
import nachos.machine.Processor;
import nachos.machine.TranslationEntry;

import java.util.Random;
import java.util.Vector;

public class TLB {
    Processor processor;
    int tlbSize;

    public TLB() {
        processor = Machine.processor();
        tlbSize = processor.getTLBSize();
        flushTLB();
    }

    public void flushTLB() {
        for (int i = 0; i < tlbSize; i++) {
            TranslationEntry tlbEntry = processor.readTLBEntry(i);
            tlbEntry.valid = false;
            processor.writeTLBEntry(i, tlbEntry);
        }
    }

    public void handleMiss(int vaddr, int processID, VMProcess vmProcess) {
        VMKernel.pageTable.sanityCheck2(vmProcess.processID, "before tlb");

        int tlbEntryNo = getTLBEntryNoToBeReplace();
        TranslationEntry replacedEntry = processor.readTLBEntry(tlbEntryNo);

        if (replacedEntry.valid && !replacedEntry.readOnly && replacedEntry.dirty)
            VMKernel.pageTable.replacePage(processID, replacedEntry);

        int vpn = Processor.pageFromAddress(vaddr);

        TranslationEntry missingEntry = VMKernel.pageTable.getPage(
                processID, vpn, vmProcess, replacedEntry);

        VMKernel.pageTable.loadPageInTLB(processID, getTLBEntryNo(replacedEntry), missingEntry);

//        System.out.println("*****Page loaded In TLB with processID: " +
//                processID + " vpn: " + missingEntry.vpn + " , ppn: " +
//                missingEntry.ppn + ", replaced vpn: " + replacedEntry.vpn);

        VMKernel.pageTable.sanityCheck2(vmProcess.processID, "after tlb");
    }

    public int getTLBEntryNo(TranslationEntry tlbEntry){
        for (int i = 0; i < tlbSize; i++) {
            TranslationEntry entry = processor.readTLBEntry(i);

            if(entry.ppn == tlbEntry.ppn && entry.vpn == tlbEntry.vpn)
                return i;
        }
        return 0;
    }

    public int getTLBEntryNoToBeReplace() {
        TranslationEntry tlbEntry;
        Random random = new Random();
        Vector<Integer> notUsedNotDirtyValues = new Vector<>(tlbSize);
        Vector<Integer> notUsedValues = new Vector<>(tlbSize);

        for (int i = 0; i < tlbSize; i++) {
            tlbEntry = processor.readTLBEntry(i);
            if (!tlbEntry.valid) {
                return i;
            }
            if (!tlbEntry.dirty && !tlbEntry.used)
                notUsedNotDirtyValues.add(i);
            else if (!tlbEntry.used)
                notUsedValues.add(i);
        }

        if (notUsedNotDirtyValues.size() != 0)
            return notUsedNotDirtyValues.get(random.nextInt(notUsedNotDirtyValues.size()));
        if (notUsedValues.size() != 0)
            return notUsedValues.get(random.nextInt(notUsedValues.size()));

        return random.nextInt(tlbSize);
    }
}
