package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.util.Hashtable;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
    /************* Start ********************/
    public static volatile TLB tlb;
    public static volatile PageTable pageTable;
    public static volatile SwapFile swapFile;
    /************* Start ********************/

    /**
     * Allocate a new VM kernel.
     */
    public VMKernel() {
        super();
    }

    /**
     * Initialize this kernel.
     */
    public void initialize(String[] args) {
        super.initialize(args);
        /************* Start ********************/
        if (tlb == null)
            tlb = new TLB();
        if (pageTable == null)
            pageTable = new PageTable(Machine.processor().getNumPhysPages());
        if(swapFile==null)
            swapFile=new SwapFile();
        /************* End ********************/
    }

    /**
     * Test this kernel.
     */
    public void selfTest() {
        super.selfTest();
    }

    /**
     * Start running user programs.
     */
    public void run() {
        super.run();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
        System.out.println("Number of page fault: "+pageTable.pageFaultCount);
        swapFile.terminate();
        super.terminate();
    }

    // dummy variables to make javac smarter
    private static VMProcess dummy1 = null;

    private static final char dbgVM = 'v';
}
