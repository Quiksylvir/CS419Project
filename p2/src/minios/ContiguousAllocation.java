package minios;

import java.util.LinkedList;

public class ContiguousAllocation implements MemoryManagement{

    private final int TOTAL_MEMORY;
    private LinkedList<Integer> freeList = new LinkedList<>();

    public ContiguousAllocation(int memory) {
        TOTAL_MEMORY = memory;
        freeList.add(memory);
    }
    @Override
    public boolean allocate(Process process) {
        return false;
    }

    @Override
    public boolean release(Process process) {
        return false;
    }

    @Override
    public boolean hasEnoughMemory(int requestedSize) {
        return false;
    }
}
