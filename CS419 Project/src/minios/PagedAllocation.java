package minios;

import java.util.Arrays;

public class PagedAllocation implements MemoryManagement{
    int[] physicalMemory;
    private final int[] frames;
    private final int PAGE_SIZE = 10;


    public PagedAllocation(int memory) {
        frames = new int[memory/PAGE_SIZE];
        initializeFrames();
        this.physicalMemory = new int[memory];
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
        System.out.println("[Tick " + currentTime + "] Process " + p.pid + " requires "  +
                (p.pageTable.length) +" frame(s) to be admitted. ");
        System.out.print("[Tick " + currentTime + "] Process " + p.pid);
        System.out.println(" found " + freeFrames + " free frame(s).");
    }

    @Override
    public int translate(Process process, int address) {
        int pageNumber = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;
        int physicalMemoryByte;

        if (translationErrorsExist(process, address, pageNumber)) {
            return -1;
        }

        physicalMemoryByte = (process.pageTable[pageNumber] * PAGE_SIZE) + offset;
        return physicalMemoryByte;
    }

    private boolean translationErrorsExist(Process process, int address, int pageNumber) {
        if (address < 0 || address >= process.memoryCost) {
            return true;
        }
        if (pageNumber >= process.pageTable.length) {
            return true;
        }
        if (process.pageTable[pageNumber] == -1) {
            return true;
        }

        return false;
    }

    @Override
    public void store(int physicalAddress, int value) {
        if (physicalAddress >= 0 && physicalAddress < physicalMemory.length) {
            physicalMemory[physicalAddress] = value;
        }
    }
    @Override
    public int load(int physicalAddress) {
        if (physicalAddress >= 0 && physicalAddress < physicalMemory.length) {
            return physicalMemory[physicalAddress];
        }
        return 0;
    }
}
