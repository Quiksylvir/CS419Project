package minios;

public interface MemoryManagement {

    public boolean allocate(Process process);

    public void release(Process process);

}
