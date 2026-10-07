package minios;

public class PagedAllocation implements MemoryManagement{

    private int[] pages;
    public PagedAllocation(int memory) {
        pages = new int[memory/4];
    }

    @Override
    public boolean allocate(Process process) {
        if (hasEnoughMemory(process)) {

        }
        return false;
    }
    @Override
    public void release(Process process) {

    }

    @Override
    public boolean hasEnoughMemory(Process process) {
        for (int x = 0; x < pages.length; x++) {

        }
        return false;
    }


    //4byte frames
    //25 size array
}
