package minios;

public interface MemoryManagement {

    boolean allocate(Process process);
    void release(Process process);

}
