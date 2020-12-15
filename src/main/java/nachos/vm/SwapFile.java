package nachos.vm;

import nachos.machine.Machine;
import nachos.machine.OpenFile;
import nachos.threads.ThreadedKernel;

import java.util.HashMap;
import java.util.LinkedList;

public class SwapFile {
    OpenFile swapFile;
    public LinkedList<Integer> freeSpace;
    public HashMap<String, Integer> allocatedPosition;

    public SwapFile() {
        swapFile = ThreadedKernel.fileSystem.open("fileforswapping", true);
        allocatedPosition = new HashMap<>();
        freeSpace = new LinkedList<>();
    }

    public int read(String key, byte[] buf, int offset) {
        if (!allocatedPosition.containsKey(key)) {
            return -1;
        }
        int position = allocatedPosition.get(key);
        position = position * Machine.processor().pageSize;
        swapFile.read(position, buf, offset, Machine.processor().pageSize);
//        allocatedPosition.remove(key);
//        freeSpace.add(position);
        return position;
    }

    public int write(String key, byte[] buf, int offset) {
        int position;
        if (allocatedPosition.containsKey(key)) {
            position = allocatedPosition.get(key);
        } else {
            if (freeSpace.isEmpty()) {
                position = allocatedPosition.size();
            } else {
                position = freeSpace.removeFirst();
            }
        }
        position = position * Machine.processor().pageSize;
        swapFile.write(position, buf, offset, Machine.processor().pageSize);
        allocatedPosition.put(key, position);
        return 0;
    }

    public void freeSpaceForAProcess(int processID){

    }


    public void terminate() {
        if (swapFile != null) {
            swapFile.close();
            ThreadedKernel.fileSystem.remove("fileforswapping");
        }
    }
}
