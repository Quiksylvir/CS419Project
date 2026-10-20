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
        int pagesRequested = process.pageTable.length;
        int pageIndex = 0;
        if (hasEnoughMemory(process)) {
            for (int x = 0; x < frames.length; x++) {
                if (frames[x] == -1) {
                    frames[x] = process.pid;
                    process.pageTable[pageIndex] = x;
                    pageIndex++;
                    pagesRequested--;
                }
                if (pagesRequested == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void release(Process process, int currentTime) {
        for (int x = 0; x < process.pageTable.length; x++) {

            frames[process.pageTable[x]] = -1;
        }
    }

    private boolean hasEnoughMemory(Process process) {
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

    @Override
    public void printMemory(int currentTime, Process p) {
        int freeFrames = 0;
        for (int frame : frames) {
            if (frame == -1) {
                freeFrames++;
            }
        }
        System.out.println("[Tick " + currentTime + "] Process " + p.pid + " requires "  + p.memoryCost +" memory to be admitted: ");
        System.out.print("[Tick " + currentTime + "] Process " + p.pid);
        System.out.println(" " + freeFrames + " frame(s) is insufficient.");
    }
}
