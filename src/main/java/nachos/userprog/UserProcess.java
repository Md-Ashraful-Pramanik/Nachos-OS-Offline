package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.VMKernel;

import javax.swing.plaf.IconUIResource;
import java.awt.print.Pageable;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public int maximumVirtualMemorySize;
    public boolean isPageAllocated = false;


    public int processId = -1;
    public int codeSectionPageCount;
    //handling graph of processes
    public Vector<UserProcess> childProcesses = new Vector<>();
    public UserProcess parentProcess = null;

    public UThread uThread;


    public static int totalNumberOfProcesses = 0;
    public static int activeProcess= 1;

    public static Hashtable<Integer, Integer> exitStatus = new Hashtable<>();

    public static Lock consoleLock = new Lock();
    public static Lock pageAllocationLock = new Lock();
    public static Lock processExecExitLock = new Lock();

    public UserProcess() {
        this.processId = totalNumberOfProcesses;
        totalNumberOfProcesses++;
    }

    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
        return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param name the name of the file containing the executable.
     * @param args the arguments to pass to the executable.
     * @return <tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
        if (!load(name, args))
            return false;

        uThread = new UThread(this);
        uThread.setName(name).fork();

        return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        //Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param vaddr     the starting virtual address of the null-terminated
     *                  string.
     * @param maxLength the maximum number of characters in the string,
     *                  not including the null terminator.
     * @return the string read, or <tt>null</tt> if no null terminator was
     * found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
        Lib.assertTrue(maxLength >= 0);

        byte[] bytes = new byte[maxLength + 1];

        int bytesRead = readVirtualMemory(vaddr, bytes);

        for (int length = 0; length < bytesRead; length++) {
            if (bytes[length] == 0)
                return new String(bytes, 0, length);
        }

        return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param vaddr the first byte of virtual memory to read.
     * @param data  the array where the data will be stored.
     * @return the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
        return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param vaddr  the first byte of virtual memory to read.
     * @param data   the array where the data will be stored.
     * @param offset the first byte to write in the array.
     * @param length the number of bytes to transfer from virtual memory to
     *               the array.
     * @return the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
                                 int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();

        /*********Start*****************/
        if (vaddr < 0)
            return -1;

        int vpn = Processor.pageFromAddress(vaddr);
        VMKernel.pageTable.getPage(processId, vpn).used = true;
        int pageOffset = Processor.offsetFromAddress(vaddr);
        int physicalAddr = Processor.makeAddress(VMKernel.pageTable.getPage(processId, vpn).ppn, pageOffset);

        System.arraycopy(memory, physicalAddr, data, offset, length);
        /*********End*****************/
        return length;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param vaddr the first byte of virtual memory to write.
     * @param data  the array containing the data to transfer.
     * @return the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
        return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param vaddr  the first byte of virtual memory to write.
     * @param data   the array containing the data to transfer.
     * @param offset the first byte to transfer from the array.
     * @param length the number of bytes to transfer from the array to
     *               virtual memory.
     * @return the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
                                  int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();

        /*********Start*****************/
        if (vaddr < 0)
            return -1;

        int vpn = Processor.pageFromAddress(vaddr);
        //System.out.println("ProcessID: "+processId+"vpn: "+vpn);
        VMKernel.pageTable.getPage(processId, vpn).used = true;
        if (VMKernel.pageTable.getPage(processId, vpn).readOnly)
            return -1;
        int pageOffset = Processor.offsetFromAddress(vaddr);
        int physicalAddr = Processor.makeAddress(VMKernel.pageTable.getPage(processId, vpn).ppn, pageOffset);

        System.arraycopy(data, offset, memory, physicalAddr, length);

        VMKernel.pageTable.getPage(processId, vpn).dirty = true;
        /*********End*****************/
        return length;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param name the name of the file containing the executable.
     * @param args the arguments to pass to the executable.
     * @return <tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
        Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

        OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
        if (executable == null) {
            Lib.debug(dbgProcess, "\topen failed");
            return false;
        }

        try {
            coff = new Coff(executable);
        } catch (EOFException e) {
            executable.close();
            Lib.debug(dbgProcess, "\tcoff load failed");
            return false;
        }

        // make sure the sections are contiguous and start at page 0
        numPages = 0;
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            if (section.getFirstVPN() != numPages) {
                coff.close();
                Lib.debug(dbgProcess, "\tfragmented executable");
                return false;
            }
            numPages += section.getLength();
        }

        // make sure the argv array will fit in one page
        byte[][] argv = new byte[args.length][];
        int argsSize = 0;
        for (int i = 0; i < args.length; i++) {
            argv[i] = args[i].getBytes();
            // 4 bytes for argv[] pointer; then string plus one for null byte
            argsSize += 4 + argv[i].length + 1;
        }
        if (argsSize > pageSize) {
            coff.close();
            Lib.debug(dbgProcess, "\targuments too long");
            return false;
        }

        // program counter initially points at the program entry point
        initialPC = coff.getEntryPoint();

        // next comes the stack; stack pointer initially points to top of it
        /**************** Start *********************/
        codeSectionPageCount = numPages;
        //System.out.println("Number of page in code section: "+codeSectionPageCount);
        /**************** End *********************/
        numPages += stackPages;
        initialSP = numPages * pageSize;

        // and finally reserve 1 page for arguments
        numPages++;

        if (!loadSections())
            return false;

        if(argc != 0){
            // store arguments in last page
            int entryOffset = (numPages - 1) * pageSize;
            /**************** Start *********************/
            VMKernel.pageTable.handlePageFault(processId, Processor.pageFromAddress(entryOffset), this);
            /**************** End ***********************/
            int stringOffset = entryOffset + args.length * 4;

            this.argc = args.length;
            this.argv = entryOffset;

            for (int i = 0; i < argv.length; i++) {
                byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
                Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
                entryOffset += 4;
                Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
                        argv[i].length);
                stringOffset += argv[i].length;
                Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[]{0}) == 1);
                stringOffset += 1;
            }
        }
        return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return <tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
//        /*********Start*****************/
//        //allocatePages();
//        if (!isPageAllocated) {
//            coff.close();
//            Lib.debug(dbgProcess, "\tinsufficient physical memory");
//            return false;
//        }
//        /*********End*****************/

        // load sections
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);

            Lib.debug(dbgProcess, "\tinitializing " + section.getName()
                    + " section (" + section.getLength() + " pages)");

            for (int i = 0; i < section.getLength(); i++) {
                int vpn = section.getFirstVPN() + i;

                // for now, just assume virtual addresses=physical addresses
                /*********Start*****************/
                if (vpn > pageTable.length)
                    return false;

                section.loadPage(i, pageTable[vpn].ppn);
                if (section.isReadOnly())
                    pageTable[vpn].readOnly = true;

                //System.out.print(pageTable[vpn].ppn + "->");

                /*********End*****************/
            }
        }

        //System.out.println();

        return true;
    }

//
//    public void allocatePages() {
//        /*********Start*****************/
//        if (UserKernel.freePages.size() > numPages) {
//            pageTable = new TranslationEntry[numPages];
//            pageAllocationLock.acquire();
//            //System.out.println("TOtal pages: "+numPages);
//
//            for (int i = 0; i < pageTable.length; i++) {
//                pageTable[i] = new TranslationEntry(i,
//                        UserKernel.freePages.pollFirst(), true, false, false, false);
//            }
//            maximumVirtualMemorySize = pageTable.length * pageSize;
//            isPageAllocated = true;
//
//            pageAllocationLock.release();
//        }
//        else {
//            System.out.println();
//            System.out.println("*********PAGE REQUIRED: " +numPages+ " ************");
//            System.out.println("*********NO of FREE PAGES: "+ UserKernel.freePages.size()+" *************");
//            System.out.println("*********NOT ENOUGH MEMORY.*****************");
//            isPageAllocated = false;
//        }
//        /*********End*****************/
//    }
//
//    /**
//     * Release any resources allocated by <tt>loadSections()</tt>.
//     */
    protected void unloadSections() {
//        /*********Start*****************/
//        if (isPageAllocated){
//            pageAllocationLock.acquire();
//            for (int i = 0; i < pageTable.length; i++)
//                UserKernel.freePages.add(pageTable[i].ppn);
//            pageAllocationLock.release();
//        }
//        /*********End*****************/
    }

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
        Processor processor = Machine.processor();

        // by default, everything's 0
        for (int i = 0; i < processor.numUserRegisters; i++)
            processor.writeRegister(i, 0);

        // initialize PC and SP according
        processor.writeRegister(Processor.regPC, initialPC);
        processor.writeRegister(Processor.regSP, initialSP);

        // initialize the first two argument registers to argc and argv
        processor.writeRegister(Processor.regA0, argc);
        processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call.
     */
    private int handleHalt() {
        if(this.parentProcess!=null)
            return -1;

        Machine.halt();

        Lib.assertNotReached("Machine.halt() did not halt machine!");
        return 0;
    }

    /***********************start*************************/
    //fileDescriptor=0 standard input
    //fileDescriptor=1 standard output
    public int handleRead(int fileDescriptor, int bufferAddress, int count) {
        if (fileDescriptor != 0 || count < 0)
            return -1;

        byte[] data = new byte[count];

        consoleLock.acquire();
        OpenFile openFile = UserKernel.console.openForReading();
        int length = openFile.read(data, 0, count);
        int writeAmount = writeVirtualMemory(bufferAddress, data, 0, length);
        openFile.close();
        consoleLock.release();

        return writeAmount;
    }

    public int handleWrite(int fileDescriptor, int bufferAddress, int count) {
        if (fileDescriptor != 1 || count < 0)
            return -1;

        byte[] data = new byte[count];

        consoleLock.acquire();
        OpenFile openFile = UserKernel.console.openForWriting();
        int length = readVirtualMemory(bufferAddress, data, 0, count);
        int writeAmount = openFile.write(data, 0, length);
        consoleLock.release();

        return writeAmount;
    }

    private int handleExec(int a0, int a1, int a2) {
        //System.out.println("********NO of FREE PAGES: "+ UserKernel.freePages.size() + " *********");
        /*********Start*****************/
        String programName = readVirtualMemoryString(a0, 1023);
        UserProcess process = newUserProcess();
        //System.out.println("Argument Count: " + a1);
        //System.out.println("Argument Starting address: " + a2);

        byte[] data = new byte[4*a1];
        int readAmount = readVirtualMemory(a2, data);
        //System.out.println(readAmount);

        String[] args = new String[a1];
        for (int i = 0; i < a1; i++) {
            int argAddress = Lib.bytesToInt(data, i*4);
            args[i] = readVirtualMemoryString(argAddress, 1024);
            //System.out.println("User process: "+args[i]);
            //System.out.println(args[i]);
            //System.out.println(args[i].length());
        }

        if(process.execute(programName, args)){
            processExecExitLock.acquire();
            activeProcess++;
            processExecExitLock.release();
        }
        else {
            System.out.println("********Program exec failed " + programName);
            return -1;
        }

        process.parentProcess = this;
        this.childProcesses.add(process);

        return process.processId;
        /*********End*****************/
    }

    private int handleJoin(int joinProcessId, int a1) {
        if(joinProcessId<0)
            return -1;

        UserProcess joiningProcess = null;
        for (UserProcess u : childProcesses) {
            if (u.processId == joinProcessId) {
                joiningProcess = u;
            }
        }

        if(joiningProcess == null
                || joiningProcess.uThread == null){
            return -1;
        }
        //System.out.println("Before JOIN");
        joiningProcess.uThread.join();
        //System.out.println("After JOIN");
        joiningProcess.parentProcess = null;

        childProcesses.remove(joiningProcess);

        return exitStatus.remove(joinProcessId);
    }

    private int handleExit(int a0) {
        processExecExitLock.acquire();
        activeProcess--;
        VMKernel.tlb.flushTLB();
        processExecExitLock.release();

        System.out.println("***************active process count: "+activeProcess+" **************");
        if(activeProcess==0)
            Kernel.kernel.terminate();

        exitStatus.put(processId, a0);

        //unloadSections();
        KThread.finish();
        return 0;
    }

    /**************************end***********************/
    private static final int
            syscallHalt = 0,
            syscallExit = 1,
            syscallExec = 2,
            syscallJoin = 3,
            syscallCreate = 4,
            syscallOpen = 5,
            syscallRead = 6,
            syscallWrite = 7,
            syscallClose = 8,
            syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     * 								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     * 								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     *
     * @param syscall the syscall number.
     * @param a0      the first syscall argument.
     * @param a1      the second syscall argument.
     * @param a2      the third syscall argument.
     * @param a3      the fourth syscall argument.
     * @return the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
        switch (syscall) {
            case syscallHalt:
                return handleHalt();
            /*************start***************/
            case syscallRead:
                return handleRead(a0, a1, a2);

            case syscallWrite:
                return handleWrite(a0, a1, a2);

            case syscallExec:
                return handleExec(a0, a1, a2);
            case syscallExit:
                return handleExit(a0);
            case syscallJoin:
                return handleJoin(a0, a1);
            default:
                Lib.debug(dbgProcess, "Unknown syscall " + syscall);

                System.out.println("***********Unknown syscall****************");
                System.out.println("***********Exiting the process****************");
                handleExit(0);

                /************end****************/
                Lib.assertNotReached("Unknown system call!");
        }
        return 0;
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
            case Processor.exceptionSyscall:
                int result = handleSyscall(processor.readRegister(Processor.regV0),
                        processor.readRegister(Processor.regA0),
                        processor.readRegister(Processor.regA1),
                        processor.readRegister(Processor.regA2),
                        processor.readRegister(Processor.regA3)
                );
                processor.writeRegister(Processor.regV0, result);
                processor.advancePC();
                break;

            default:
                System.out.println("Unexpected exception: " + Processor.exceptionNames[cause]);
                if(Processor.exceptionNames[cause].toString().startsWith("read-only"))
                    Lib.assertNotReached();
                Lib.debug(dbgProcess, "Unexpected exception: " +
                        Processor.exceptionNames[cause]);
                System.out.println("************Unknown exception******************");
                System.out.println("************Exiting the process******************");
                handleExit(0);
                Lib.assertNotReached("Unexpected exception "+Processor.exceptionNames[cause]);
        }
    }

    /**
     * The program being run by this process.
     */
    public Coff coff;

    /**
     * This process's page table.
     */
    protected TranslationEntry[] pageTable;
    /**
     * The number of contiguous pages occupied by the program.
     */
    protected int numPages;

    /**
     * The number of pages in the program's stack.
     */
    protected final int stackPages = 8;

    private int initialPC, initialSP;
    private int argc, argv;

    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
}
