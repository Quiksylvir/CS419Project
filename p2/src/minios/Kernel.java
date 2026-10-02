package minios;

import java.util.*;

public class Kernel {
    private final SchedulingAlgo algo;
    private final List<Process> readyQueue = new ArrayList<>();
    private final List<Process> waitQueue = new ArrayList<>();
    private final List<Process> memoryQueue = new ArrayList<>();
    private final Map<Integer,Integer> startTimes = new HashMap<>();
    private final Map<Integer, Integer> timesPreempted = new HashMap<>();
    private final ContiguousAllocation MemoryManager;
    private Process runningProcess = null;


    private int quantumLeft;
    private final int TIME_QUANTUM;


    public Kernel(SchedulingAlgo algo, int timeQuantum) {
        this.algo = algo;
        this.TIME_QUANTUM = timeQuantum;
        this.quantumLeft = timeQuantum;
        MemoryManager = new ContiguousAllocation(100);
    }

    public void admitProcess(Process p) {
        //decide if there is enough memory
        //if (enoughmemorycontiguous) {} if yes, do below and allocate the space (select the first contiguous region)
        if (MemoryManager.allocate(p)) {
            p.state = Process.State.READY;
            algo.addProcess(readyQueue, p);
        } else {
            p.state = Process.State.NEW;
            memoryQueue.add(p);
            //if no, Process.State.New/Dont change to ready or place in readyQueue
            //create a new queue and place in that queue
        }


    }

    // Called on every clock tick
    public void onClockTick(int currentTime) {
        // check all processes currently waiting in I/O wait queue
        serviceWaitQueue(currentTime);

        boolean cpuCycleUsed = false;
        // Execute one CPU cycle
        // (One clock tick is one CPU cycle)
        while (!cpuCycleUsed) {
            // No process currently running
            if (runningProcess == null) {
                if (dispatchNextProcess(currentTime) == null){
                    // No more process to schedule; simulation finishes.
                    break;
                }
            }

            Instruction inst = runningProcess.getCurrentInstruction();

            // Current process has finished.
            if (inst == null) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " Terminates.");
                terminateProcess(runningProcess);
                // In this case, no instruction is executed, so no CPU cycle
                // is used. Can't advance the simulation clock. Loop
                // back and grab the next process to execute.
            }

            // I/O instruction: process enters I/O and no longer uses CPU
            // (Assume this transition itself doesn't consume CPU cycles.)
            else if (inst.type != Instruction.OpType.CPU) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " enters I/O wait.");
                runningProcess.state = Process.State.BLOCKED;
                waitQueue.add(runningProcess);
                runningProcess = null;
                // Can't advance the simulation clock. Loop back
                // and grab the next process to execute.
            }

            // CPU instruction: the instruction has not finished;
            // Utilize one CPU cycle
            else if(inst.remainingTicks > 0){
                inst.remainingTicks--;
                cpuCycleUsed = true;
                quantumLeft--;
                if (inst.remainingTicks == 0) {
                    // The instruction now finishes; load the next
                    // instruction.
                    runningProcess.programCounter++;
                }

                if (quantumLeft == 0) {
                    if (runningProcess.getCurrentInstruction() != null) {
                        System.out.println("[Tick " + (currentTime+1) + "] Process " + runningProcess.pid + " time quantum expired. Preempting.");
                        markPreemptCounter(runningProcess);
                        runningProcess.state = Process.State.READY;
                        algo.addProcess(readyQueue,runningProcess);
                        runningProcess = null;
                    }
                }
            }

            // CPU instruction: the instruction has finished
            // (should never come here as this is already handled above,
            // but just in case...)
            else{
                runningProcess.programCounter++;
                // Can't advance the simulation clock. Loop back
                // and move to the next instruction.
            }
        }
    }

    private void markPreemptCounter(Process runningProcess) {
        if (timesPreempted.containsKey(runningProcess.pid)) {
            timesPreempted.put(runningProcess.pid, (timesPreempted.get(runningProcess.pid) + 1));
        } else {
            timesPreempted.put(runningProcess.pid, 1);
        }
    }

    public Map<Integer, Integer> getTimesPreempted() {
        return timesPreempted;
    }

    private void serviceWaitQueue(int currentTime) {
        Iterator<Process> it = waitQueue.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            Instruction inst = p.getCurrentInstruction();
            if (inst != null) {
                inst.remainingTicks--;
                if (inst.remainingTicks <= 0) {
                    // The I/O wait has completed;
                    // load the next instruction.
                    System.out.println("[Tick " + currentTime + "] Process " + p.pid + " I/O completes.");
                    p.programCounter++;
                    p.state = Process.State.READY;
                    // Move the process to Ready Queue
                    algo.addProcess(readyQueue, p);
                    it.remove();
                }
            }
        }
    }

    private void terminateProcess(Process p) {
        //TO-DO MMU release memory
        MemoryManager.release(p);
        quantumLeft = TIME_QUANTUM;
        p.state = Process.State.TERMINATED;
        runningProcess = null;


        Iterator<Process> iterator = memoryQueue.iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            if (MemoryManager.hasEnoughMemory(process.memoryCost)) {
                readyQueue.add(process);
                iterator.remove();
            }
        }
    }

    private Process dispatchNextProcess(int currentTime) {
        runningProcess = algo.selectNextProcess(readyQueue);
        if (runningProcess != null) {
            quantumLeft = TIME_QUANTUM;
            if (TIME_QUANTUM == 0) {
                addInitialStartTime(currentTime, runningProcess.pid);
            } else {
                addLastStartTime(currentTime, runningProcess.pid);
            }
            System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " executes.");
            runningProcess.state = Process.State.RUNNING;
            return runningProcess;
        }else{
            return null;
        }

    }

    public boolean isIdle() {
        return runningProcess == null &&
                readyQueue.isEmpty() &&
                waitQueue.isEmpty();
    }


    private void addInitialStartTime(int currentTime, int pid) {
            if (!startTimes.containsKey(pid)) {
                startTimes.put(pid, currentTime);
            }
    }

    private void addLastStartTime(int currentTime, int pid) {
        startTimes.put(pid,currentTime);
    }

    public Map<Integer, Integer> getStartTimes() {
        return startTimes;
    }
}