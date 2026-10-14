package minios;

import java.util.Arrays;

public class PagedAllocation implements MemoryManagement{

    private final int[] frames;
    public PagedAllocation(int memory) {
        frames = new int[memory/4];
        initializeFrames();
    }

    private void initializeFrames() {
        Arrays.fill(frames, -1);
    }

    @Override
    public boolean allocate(Process process) {
        int pagesRemaining = process.pageTable.length;
        int pageIndex = 0;
        if (hasEnoughMemory(process)) {
            for (int x = 0; x < frames.length; x++) {
                if (frames[x] == -1) {
                    frames[x] = process.pid;
                    process.pageTable[pageIndex] = x;
                    pageIndex++;
                    pagesRemaining--;
                }
                if (pagesRemaining == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void release(Process process) {
        for (int x = 0; x < process.pageTable.length; x++) {
            frames[process.pageTable[x]] = -1;
        }
    }

    @Override
    public boolean hasEnoughMemory(Process process) {
        int sizeCounter = 0;
        for (int page : frames) {
            if (page == -1) {
                sizeCounter += 1;
            }
            if (sizeCounter == process.pageTable.length) {
                return true;
            }
        }
        return false;
    }


    //4byte frames
    //25 size array
}
