package minios;

public interface MemoryManagement {

    boolean allocate(Process process);
    void release(Process process, int currentTime);
    void printMemory(int currentTime, Process process);

}
