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

                block.startAddress = block.startAddress + process.memoryCost;
                block.isFree = false;
                block.size = process.memoryCost;
                block.processId = process.pid;

                if (remainingSize > 0) {
                    MemoryBlock leftover = new MemoryBlock(block.startAddress, remainingSize, true);
                    freeList.addLast(leftover);
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
        coalesce();
    }

    private void coalesce() {
        for (int i = 0; i < freeList.size() - 1; i++) {
            MemoryBlock current = freeList.get(i);
            MemoryBlock next = freeList.get(i + 1);

            if (current.isFree && next.isFree) {
                current.size += next.size;
                freeList.remove(i + 1);
                i--;
            }
        }
    }

    @Override
    public void printMemory(int currentTime, Process p) {
        System.out.println("[Tick " + currentTime + "] Process " + p.pid + " requires "  + p.memoryCost +" memory to be admitted: ");
        for (MemoryBlock block: freeList) {
            if(block.isFree) {

                System.out.print("[Tick " + currentTime + "] Process " + p.pid);
                System.out.println(" block Size " + block.size + " is insufficient.");
            }
        }
    }
}

