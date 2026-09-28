package minios;

import java.util.ArrayList;
import java.util.List;

public class MMU {

    private final int TOTAL_MEMORY;
    private final List<Integer> memoryBlock = new ArrayList<>();

    public MMU(int memory) {
        TOTAL_MEMORY = memory;
        initializeMemoryBlock();
    }

    private void initializeMemoryBlock() {
        for (int x = 0; x < TOTAL_MEMORY; x++) {
            memoryBlock.add(-1);
        }
    }

    public boolean getMemoryBlock(int requestedMemorySize, int pid) {
        int positionTracker = 0;
        int sizeCounter = 0;

        for (int x = 0; x < TOTAL_MEMORY; x++) {
            if (memoryBlock.get(x) == -1) {
                sizeCounter++;
            } else {
                sizeCounter = 0;
                positionTracker = x + 1;
            }
            if (sizeCounter == requestedMemorySize) {
                for (int y = positionTracker; y < (requestedMemorySize + positionTracker); y++) {
                    memoryBlock.set(y, pid);
                }
                System.out.println(memoryBlock);
                return true;
            }
        }
        return false;
    }

    public boolean hasEnoughMemory(int requestedMemorySize)  {
        int sizeCounter = 0;

        for (int x = 0; x < TOTAL_MEMORY; x++) {
            if (memoryBlock.get(x) == -1) {
                sizeCounter++;
            } else {
                sizeCounter = 0;
            }
            if (sizeCounter == requestedMemorySize) {
                return true;
            }
        }
        return false;
    }

    public void releaseMemoryBlock(int pid) {
        for (int x = 0; x < TOTAL_MEMORY; x++) {
            if (memoryBlock.get(x) == pid) {
                memoryBlock.set(x, -1);
            }
        }
    }
    //must do ---
    //Keep track of free memory regions vs. occupied
    //allocates memory space to processes (pid)
    //release memory space to be free within Kernel.terminate
}
