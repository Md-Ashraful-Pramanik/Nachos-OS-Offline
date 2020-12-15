package nachos.vm;

import nachos.machine.Machine;
import nachos.machine.OpenFile;
import nachos.threads.ThreadedKernel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class SwapFile {
    OpenFile swapFile;
    public LinkedList<Integer> freeSpace;
    public Hashtable<String, Integer> allocatedPosition;

    public int pageSize;

    public SwapFile() {
        swapFile = ThreadedKernel.fileSystem.open("SwapSpace", true);
        allocatedPosition = new Hashtable<>();
        freeSpace = new LinkedList<>();
        pageSize = Machine.processor().pageSize;
    }

    public int read(String key, byte[] buf, int offset) {
        if (!allocatedPosition.containsKey(key)) {
            return -1;
        }
        int index = allocatedPosition.get(key);
        int position = index * pageSize;
        swapFile.read(position, buf, offset, pageSize);
        return position;
    }

    public int write(String key, byte[] buf, int offset) {
        int index;
        if (allocatedPosition.containsKey(key)) {
            index = allocatedPosition.get(key);
        } else {
            if (freeSpace.isEmpty()) {
                index = allocatedPosition.size();
            } else {
                index = freeSpace.removeFirst();
            }
            allocatedPosition.put(key, index);
        }
        int position = index * pageSize;
        swapFile.write(position, buf, offset, pageSize);
        return 0;
    }

    public void freeSpaceForAProcess(int processID){

    }

    public void terminate() {
        if (swapFile != null) {
            swapFile.close();
            ThreadedKernel.fileSystem.remove("SwapSpace");
        }
    }
}
