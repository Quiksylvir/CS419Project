package minios;

public class MMU {

    private final int TOTAL_MEMORY;
    private final int[] memoryBlock;

    public MMU(int memory) {
        TOTAL_MEMORY = memory;
        memoryBlock = new int[memory];
        initializeMemoryBlock();
    }

    private void initializeMemoryBlock() {
        for (int x = 0; x < TOTAL_MEMORY; x++) {
            memoryBlock[x] = -1;
        }
    }

    //returns true if allocation was successful and false if it was not
    public boolean getMemoryBlock(int requestedMemorySize, int pid) {
        int startIndexTracker = 0;
        int contiguousSizeLocated = 0;

            /* Reads from Memory */
        for (int readIndex = 0; readIndex < TOTAL_MEMORY; readIndex++) {
            if (memoryBlock[readIndex] == -1) {
                contiguousSizeLocated++;
            } else {
                contiguousSizeLocated = 0;
                startIndexTracker = readIndex + 1;
            }
            /* End Reads from Memory */

            /* Writes to Memory */
            if (contiguousSizeLocated == requestedMemorySize) {
                for (int writeIndex = startIndexTracker; writeIndex < (requestedMemorySize + startIndexTracker); writeIndex++) {
                    memoryBlock[writeIndex] = pid;
                }
                return true;
            }
            /* End Writes to Memory */
        }
        return false;
    }

    public boolean hasEnoughMemory(int requestedMemorySize)  {
        int sizeCounter = 0;

        for (int x = 0; x < TOTAL_MEMORY; x++) {
            if (memoryBlock[x] == -1) {
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
            if (memoryBlock[x] == pid) {
                memoryBlock[x] = -1;
            }
        }
    }
}
