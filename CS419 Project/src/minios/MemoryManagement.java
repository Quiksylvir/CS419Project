package minios;

public interface MemoryManagement {

    boolean allocate(Process process);
    void release(Process process, int currentTime);
    void printMemory(int currentTime, Process process);
    int translate(Process process, int address);

    void store(int physicalAddr, int valueToStore);

    int load(int physicalAddr);
}
