package minios;

import java.util.ArrayList;
import java.util.List;

public class Process {
    public enum State { NEW, READY, RUNNING, BLOCKED, TERMINATED }

    public final int pid;
    public final int arrivalTime;
    public final int memoryCost;
    public State state = State.NEW;
    public final List<Instruction> code;
    public int programCounter = 0;
    public int baseAddress = 0;
    public int[] pageTable;

    //size (limit register): read from the tracer
    //relocation register (base memory address) : specified by kernel
    public Process(int pid, int arrivalTime, int currentMemory, List<Instruction> code) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.memoryCost = currentMemory;
        this.code = new ArrayList<>(code);
        pageTable = new int[memoryCost/4];
    }

    public Instruction getCurrentInstruction() {
        if (programCounter < code.size()){
            return code.get(programCounter);
        }else{
            return null;
        }
    }


}