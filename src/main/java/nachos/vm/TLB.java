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
        TranslationEntry tlbEntry;
        for (int i = 0; i < tlbSize; i++) {
            tlbEntry = processor.readTLBEntry(i);
            tlbEntry.valid = false;
        }
    }

    public void saveInPageTable(int processID) {
        TranslationEntry tlbEntry;
        for (int i = 0; i < tlbSize; i++) {
            tlbEntry = processor.readTLBEntry(i);
            if(tlbEntry.valid && !tlbEntry.readOnly && tlbEntry.dirty)
                VMKernel.pageTable.replacePage(processID, tlbEntry);
        }
    }

    public void loadPageInTLB(int vaddr, int processID, VMProcess vmProcess) {
        int tlbEntryNo = getTLBEntryNoToBeReplace();
        TranslationEntry tlbEntry = processor.readTLBEntry(tlbEntryNo);

        if (tlbEntry.valid && !tlbEntry.readOnly && tlbEntry.dirty)
            VMKernel.pageTable.replacePage(processID, tlbEntry);

        int vpn = Processor.pageFromAddress(vaddr);
        if (!VMKernel.pageTable.containsPage(processID, vpn)){
            VMKernel.pageTable.handlePageFault(processID, vpn, vmProcess, tlbEntry);
        }

        TranslationEntry missingEntry = VMKernel.pageTable.getPage(processID, vpn);
        processor.writeTLBEntry(tlbEntryNo, missingEntry);
        //System.out.println("*****Page loaded In TLB with processID: "+processID+" vpn: "+missingEntry.vpn+" , ppn: "+missingEntry.ppn);
    }

    public int getTLBEntryNoToBeReplace() {
        TranslationEntry tlbEntry;
        Random random = new Random();
        Vector<Integer> invalidValues = new Vector<>(tlbSize);
        Vector<Integer> notUsedNotDirtyValues = new Vector<>(tlbSize);
        Vector<Integer> notUsedValues = new Vector<>(tlbSize);

        for (int i = 0; i < tlbSize; i++) {
            tlbEntry = processor.readTLBEntry(i);
            if (!tlbEntry.valid)
                invalidValues.add(i);
            if (!tlbEntry.dirty && !tlbEntry.used)
                notUsedNotDirtyValues.add(i);
            else if (!tlbEntry.used)
                notUsedValues.add(i);
        }

        if (invalidValues.size() != 0)
            return invalidValues.get(random.nextInt(invalidValues.size()));
        if (notUsedNotDirtyValues.size() != 0)
            return notUsedNotDirtyValues.get(random.nextInt(notUsedNotDirtyValues.size()));
        if (notUsedValues.size() != 0)
            return notUsedValues.get(random.nextInt(notUsedValues.size()));

        return random.nextInt(tlbSize);
    }
}
