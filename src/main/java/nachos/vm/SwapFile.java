package nachos.vm;

import nachos.machine.Machine;
import nachos.machine.OpenFile;
import nachos.machine.Processor;
import nachos.threads.ThreadedKernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class SwapFile {
    OpenFile swapFile;
    public LinkedList<Integer> freeSpace;
    public Hashtable<String, Integer> allocatedPosition;
    public Hashtable<String, byte[]> swapSpace;
    public static int pageSize;

    public SwapFile() {
        swapFile = ThreadedKernel.fileSystem.open("SwapSpace", true);
        allocatedPosition = new Hashtable<>();
        freeSpace = new LinkedList<>();
        pageSize = Processor.pageSize;
        swapSpace = new Hashtable<>();
    }

    public boolean read(String key, int ppn) {
        if(!swapSpace.containsKey(key))
            return false;

        int offset = ppn * pageSize;
        byte[] memory = Machine.processor().getMemory();
        byte[] bytes = swapSpace.get(key);
        System.arraycopy(bytes, 0, memory, offset, pageSize);
        
        //System.out.println("Read: "+key+" PPN: "+ppn);
        //System.out.println(new String(swapSpace.get(key)));
        return true;
//        if (!allocatedPosition.containsKey(key)) {
//            return -1;
//        }
//        int index = allocatedPosition.get(key);
//        int position = index * pageSize;
//        swapFile.read(position, memory, offset, pageSize);
//        return position;
    }

    public boolean write(String key, int ppn) {
        int offset = ppn * pageSize;
        byte[] memory = Machine.processor().getMemory();
        byte[] bytes = new byte[pageSize];
        System.arraycopy(memory, offset, bytes, 0, pageSize);
        swapSpace.put(key, bytes);
        //System.out.println("Write: "+key);
       // System.out.println(new String(swapSpace.get(key)));
        return true;
//        int index;
//        if (allocatedPosition.containsKey(key)) {
//            index = allocatedPosition.get(key);
//        } else {
//            if (freeSpace.isEmpty()) {
//                index = allocatedPosition.size();
//            } else {
//                index = freeSpace.removeFirst();
//            }
//            allocatedPosition.put(key, index);
//        }
//        int position = index * pageSize;
//        swapFile.write(position, memory, offset, pageSize);
//        return 0;
    }

    public void freeSpaceForAProcess(int processID) {

    }

    public void terminate() {
        if (swapFile != null) {
            swapFile.close();
            ThreadedKernel.fileSystem.remove("SwapSpace");
        }
    }
}
