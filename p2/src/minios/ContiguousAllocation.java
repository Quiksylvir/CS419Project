package minios;

import java.util.LinkedList;

public class ContiguousAllocation implements MemoryManagement{

    static class MemoryBlock {
        int startAddress;
        int size;
        boolean isFree;
        int processId = -1;

        MemoryBlock(int startAddress, int size, boolean isFree) {
            this.startAddress = startAddress;
            this.size = size;
            this.isFree = isFree;
        }
    }

    private final LinkedList<MemoryBlock> freeList;

    public ContiguousAllocation(int memory) {
        this.freeList= new LinkedList<>();
        freeList.add(new MemoryBlock(0, memory, true));
    }

    @Override
    public boolean allocate(Process process) {
        for (int i = 0; i < freeList.size(); i++) {
            MemoryBlock block = freeList.get(i);

            if (block.isFree && block.size >= process.memoryCost) {
                process.baseAddress = block.startAddress;
                int remainingSize = block.size - process.memoryCost;

                block.isFree = false;
                block.size = process.memoryCost;
                block.processId = process.pid;

                if (remainingSize > 0) {
                    int leftoverStart = block.startAddress + process.memoryCost;
                    MemoryBlock leftover = new MemoryBlock(leftoverStart, remainingSize, true);
                    freeList.add(i+1,leftover);
                }
                return true;
            }

        }

        return false;
    }

    @Override
    public void release(Process process, int currentTime){
        for (MemoryBlock block : freeList) {
            if (block.processId == process.pid) {
                block.isFree = true;
                block.processId = -1;
                break;
            }
        }
        coalesce(currentTime);
    }

    private void coalesce(int currentTime) {
        boolean didCoalesce = false;

        for (int i = 0; i < freeList.size() - 1; i++) {
            MemoryBlock current = freeList.get(i);
            MemoryBlock next = freeList.get(i + 1);

            if (current.isFree && next.isFree) {
                didCoalesce = true;
                System.out.println("[Tick " + currentTime + "] Memory coalesces.");
                for (MemoryBlock block: freeList) {
                    if(block.isFree) {
                        System.out.print("[Tick " + currentTime + "] Memory");
                        System.out.println(" before coalesce [Start Address-" + block.startAddress + "] [Size-" + block.size +"]");
                    }
                }
                current.size += next.size;
                freeList.remove(i + 1);
                i--;
            }
        }
        if (didCoalesce){
        for (MemoryBlock block: freeList) {
            if(block.isFree) {
                System.out.print("[Tick " + currentTime + "] Memory");
                System.out.println(" after coalesce [Start Address-" + block.startAddress + "] [Size-" + block.size + "]");
            }
        }
        }

    }

    @Override
    public void printMemory(int currentTime, Process p) {
        System.out.println("[Tick " + currentTime + "] Process " + p.pid + " requires " +
                "[Size-"  + p.memoryCost +"] memory to be admitted: ");

        for (MemoryBlock block: freeList) {
            if(block.isFree) {

                System.out.print("[Tick " + currentTime + "] Process " + p.pid);
                System.out.println(" Memory [Size-" + block.size + "] is insufficient.");
            }
        }
    }
}

