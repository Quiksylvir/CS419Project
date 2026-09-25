package minios;

import java.util.ArrayList;
import java.util.List;

public class Process {
    public enum State { NEW, READY, RUNNING, BLOCKED, TERMINATED }

    public final int pid;
    public final int arrivalTime;
    public final int currentMemory;
    public State state = State.NEW;
    public final List<Instruction> code;
    public int programCounter = 0;

    //size (limit register): read from the tracer
    //relocation register (base memory address) : specified by kernel
    public Process(int pid, int arrivalTime, int currentMemory, List<Instruction> code) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.currentMemory = currentMemory;
        this.code = new ArrayList<>(code);
    }

    public Instruction getCurrentInstruction() {
        if (programCounter < code.size()){
            return code.get(programCounter);
        }else{
            return null;
        }
    }


}