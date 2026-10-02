package minios;

public interface MemoryManagement {
    public boolean allocate(Process process);

    public boolean release(Process process);

    public boolean hasEnoughMemory(int requestedSize);
}
