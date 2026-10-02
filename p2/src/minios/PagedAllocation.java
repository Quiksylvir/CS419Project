package minios;

public class PagedAllocation implements MemoryManagement{
    public PagedAllocation(int memory) {
    }

    @Override
    public boolean allocate(Process process) {
        return false;
    }

    @Override
    public void release(Process process) {

    }

    //5byte frames
}
