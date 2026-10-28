package minios;

public class Instruction {
    public enum OpType { COMPUTE, LOAD, STORE, IO }

    public final OpType type;
    public int duration;
    public int remainingTicks;
    public int address;   // used by LOAD and STORE only
    public int value;     // used by STORE only

    // For COMPUTE and IO
    public Instruction(OpType type, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be > 0. Got: " + duration);
        }
        this.type = type;
        this.duration = duration;
        this.remainingTicks = duration;
    }

    // For LOAD and STORE (duration is always 1)
    public Instruction(OpType type, int address, int value) {
        this.type = type;
        this.duration = 1;
        this.remainingTicks = 1;
        this.address = address;
        this.value = value;
    }
}