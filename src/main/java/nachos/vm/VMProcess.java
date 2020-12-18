package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.util.Arrays;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {

    public static Lock lock = new Lock();
    byte[] mem = null;

    /**
     * Allocate a new process.
     */
    public VMProcess() {
        super();
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {

        VMKernel.pageTable.sanityCheck2(processID, "before save");
        VMKernel.pageTable.saveTLB(processID);
        VMKernel.tlb.flushTLB();
//        mem = new byte[4096];
//        System.arraycopy(Machine.processor().getMemory(), 0, mem, 0, 4096);

        super.saveState();
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        VMKernel.pageTable.restoreTLB(processID);
//        if (mem != null && !Arrays.equals(mem, Machine.processor().getMemory())) {
//            System.out.println("###########Not");
//        }
        super.restoreState();

        VMKernel.pageTable.sanityCheck2(processID, "after restore");
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     *
     * @return <tt>true</tt> if successful.
     */
    protected boolean loadSections() {
        //return super.loadSections();
        return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        super.unloadSections();
    }

    @Override
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
                                 int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();

        /*********Start*****************/
        if (vaddr < 0)
            return -1;

        TranslationEntry entry = getTranslationEntry(vaddr);
        entry.used = true;
        int physicalAddr = getPhysicalAddress(entry, vaddr);
        System.arraycopy(memory, physicalAddr, data, offset, length);
        /*********End*****************/
        return length;
    }

    @Override
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
                                  int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();

        /*********Start*****************/
        if (vaddr < 0)
            return -1;

        TranslationEntry entry = getTranslationEntry(vaddr);
        if (entry.readOnly) return -1;
        entry.used = true;
        entry.dirty = true;
        int physicalAddr = getPhysicalAddress(entry, vaddr);

        System.arraycopy(data, offset, memory, physicalAddr, length);
        /*********End*****************/
        return length;
    }

    public TranslationEntry getTranslationEntry(int vaddr) {
        int vpn = Processor.pageFromAddress(vaddr);

        while (true) {
            for (int i = 0; i < Machine.processor().getTLBSize(); i++) {
                if (Machine.processor().readTLBEntry(i).valid &&
                        Machine.processor().readTLBEntry(i).vpn == vpn) {
                    return Machine.processor().readTLBEntry(i);
                }
            }

            handleTLBMiss(vaddr);
        }
    }

    public int getPhysicalAddress(TranslationEntry entry, int vaddr) {
        int pageOffset = Processor.offsetFromAddress(vaddr);
        return Processor.makeAddress(entry.ppn, pageOffset);
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param cause the user exception that occurred.
     */
    public void handleException(int cause) {
        Processor processor = Machine.processor();

        switch (cause) {
            case Processor.exceptionTLBMiss:
                handleTLBMiss(processor.readRegister(Processor.regBadVAddr));
                break;
            default:
                super.handleException(cause);
                break;
        }
    }

    public void handleTLBMiss(int vaddr) {
        lock.acquire();
        VMKernel.tlb.handleMiss(vaddr, processID, this);
        lock.release();
    }

    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private static final char dbgVM = 'v';
}
