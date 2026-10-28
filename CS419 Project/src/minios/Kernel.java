package minios;

import java.util.*;

public class Kernel {
    private final SchedulingAlgo algo;
    private final List<Process> readyQueue = new ArrayList<>();
    private final List<Process> waitQueue = new ArrayList<>();
    private final List<Process> memoryQueue = new ArrayList<>();
    private final MemoryManagement MemoryManager;
    private Process runningProcess = null;
    private final Map<Integer, Integer> readyQueueWaitTimes = new HashMap<>();
    private final Map<Integer, Integer> memoryQueueWaitTimes = new HashMap<>();
    private int quantumLeft;
    private final int TIME_QUANTUM;

    public Kernel(SchedulingAlgo algo, int timeQuantum, int memoryModeFlag) {
        this.algo = algo;
        this.TIME_QUANTUM = timeQuantum;
        this.quantumLeft = timeQuantum;
        if (memoryModeFlag == 0) {
            MemoryManager = new ContiguousAllocation(120);
        } else {
            MemoryManager = new PagedAllocation(120);
        }
    }

    public void admitProcess(Process p, int currentTime) {
        if (MemoryManager.allocate(p)) {
            declareMemoryAllocated(p, currentTime);
            p.state = Process.State.READY;
            algo.addProcess(readyQueue, p);
        } else {
            MemoryManager.printMemory(currentTime, p);
            System.out.println("[Tick " + currentTime + "] Process " + p.pid + " enters memory queue.");
            p.state = Process.State.NEW;
            memoryQueue.add(p);
            //if no, Process.State.New/Dont change to ready or place in readyQueue
            //create a new queue and place in that queue
        }


    }

    private static void declareMemoryAllocated(Process p, int currentTime) {
        if (p.baseAddress != null) {
            System.out.print("[Tick " + currentTime + "] Process " + p.pid);
            System.out.println(" [Start Address-" + p.baseAddress + "] [Size " + p.memoryCost + "]");
        } else {
            System.out.print("[Tick " + currentTime + "] Process " + p.pid);
            System.out.println(" Page Table:" + Arrays.toString(p.pageTable));
        }
    }

    // Called on every clock tick
    public void onClockTick(int currentTime) {
        trackReadyQueueWaitTimes();
        trackMemoryQueueWaitTimes();
        // check all processes currently waiting in I/O wait queue
        serviceIOWaitQueue(currentTime);
        boolean cpuCycleUsed = false;
        // Execute one CPU cycle
        // (One clock tick is one CPU cycle)
        while (!cpuCycleUsed) {
            // No process currently running
            if (runningProcess == null) {
                if (dispatchNextProcess(currentTime) == null) {
                    // No more process to schedule; simulation finishes.
                    break;
                }
            }

            Instruction inst = runningProcess.getCurrentInstruction();

            // Current process has finished.
            if (inst == null) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " Terminates.");
                terminateProcess(runningProcess, currentTime);
                continue;
                // In this case, no instruction is executed, so no CPU cycle
                // is used. Can't advance the simulation clock. Loop
                // back and grab the next process to execute.
            }

            //Find out if we have cpu or io instruction
            boolean isCpuInstruction = (inst.type == Instruction.OpType.COMPUTE ||
                    inst.type == Instruction.OpType.STORE ||
                    inst.type == Instruction.OpType.LOAD);

            // I/O instruction: process enters I/O and no longer uses CPU
            // (Assume this transition itself doesn't consume CPU cycles.)
            if (!isCpuInstruction) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " enters I/O wait.");
                runningProcess.state = Process.State.BLOCKED;
                waitQueue.add(runningProcess);
                runningProcess = null;
                continue;
                // Can't advance the simulation clock. Loop back
                // and grab the next process to execute.
            }

            // CPU instruction: the instruction has not finished;
            // Utilize one CPU cycle

            // TO-DO:: ADD IN STORE AND LOAD.. THE UNDER FUNCTION WILL BE COMPUTE JUST RENAMED
            if (inst.type == Instruction.OpType.STORE || inst.type == Instruction.OpType.LOAD) {
                int virtualAddr = inst.address;
                int physicalAddr = MemoryManager.translate(runningProcess, virtualAddr);

                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid +
                        " [Virtual Address: " + virtualAddr + "] [Physical Address: " + physicalAddr + "]");

                if (physicalAddr == -1) {
                    cpuCycleUsed = true;
                    quantumLeft--;

                    System.out.println("[Tick " + currentTime + "] WARNING: Invalid address access by Process "
                            + runningProcess.pid + " with instruction " + inst.type + " at Address " + virtualAddr);
                    System.err.println("[Tick " + currentTime + "] WARNING: Invalid address access by Process "
                            + runningProcess.pid + " with instruction " + inst.type + " at Address " + virtualAddr);

                    terminateProcess(runningProcess, currentTime);
                    return;
                }

                //Store Operation
                if (inst.type == Instruction.OpType.STORE) {
                    int valueToStore = inst.value;
                    MemoryManager.store(physicalAddr, valueToStore);
                    //Load Operation
                } else if (inst.type == Instruction.OpType.LOAD) {
                    int valueLoaded = MemoryManager.load(physicalAddr);
                    System.out.println("[Tick " + currentTime + "] " + runningProcess.pid
                            + " Value Loaded: " + valueLoaded);
                }


                cpuCycleUsed = true;
                quantumLeft--;
                runningProcess.programCounter++;

                //NOT DOING RR2 FOR THIS ITERATION
                if (quantumLeft == 0 && runningProcess.getCurrentInstruction() != null) {
                    System.out.println("[Tick " + (currentTime + 1) + "] Process " + runningProcess.pid + " time quantum expired. Preempting.");
                    runningProcess.state = Process.State.READY;
                    algo.addProcess(readyQueue, runningProcess);
                    runningProcess = null;
                }
            //Compute Operation
            }  else if (inst.remainingTicks > 0) {
                inst.remainingTicks--;
                cpuCycleUsed = true;
                quantumLeft--;

                if (inst.remainingTicks == 0) {
                    runningProcess.programCounter++;
                }

                if (quantumLeft == 0 && runningProcess.getCurrentInstruction() != null) {
                    System.out.println("[Tick " + (currentTime + 1) + "] Process " + runningProcess.pid + " time quantum expired. Preempting.");
                    runningProcess.state = Process.State.READY;
                    algo.addProcess(readyQueue, runningProcess);
                    runningProcess = null;
                }
            }
            // CPU instruction: the instruction has finished
            // (should never come here as this is already handled above,
            // but just in case...)
            else {
                runningProcess.programCounter++;
                // Can't advance the simulation clock. Loop back
                // and move to the next instruction.
            }
        }
    }

    private void trackMemoryQueueWaitTimes() {
        for (Process process : memoryQueue) {
            if (memoryQueueWaitTimes.containsKey(process.pid)) {
                memoryQueueWaitTimes.put(process.pid, (memoryQueueWaitTimes.get(process.pid) + 1));
            } else {
                memoryQueueWaitTimes.put(process.pid, 0);
            }
        }
    }

    private void trackReadyQueueWaitTimes() {
        for (Process process : readyQueue) {
            if (readyQueueWaitTimes.containsKey(process.pid)) {
                readyQueueWaitTimes.put(process.pid, (readyQueueWaitTimes.get(process.pid) + 1));
            } else {
                readyQueueWaitTimes.put(process.pid, 0);
            }
        }
    }

    private void serviceIOWaitQueue(int currentTime) {
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

    private void terminateProcess(Process p, int currentTime) {
        verifyHashMapsAreNotBlank(p);
        MemoryManager.release(p, currentTime);
        quantumLeft = TIME_QUANTUM;
        p.state = Process.State.TERMINATED;
        runningProcess = null;

        Iterator<Process> iterator = memoryQueue.iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            if (MemoryManager.allocate(process)) {
                System.out.println("[Tick " + currentTime + "] Process " + process.pid + " can be admitted from memory queue.");
                declareMemoryAllocated(process, currentTime);
                readyQueueWaitTimes.put(process.pid, 0);
                algo.addProcess(readyQueue, process);
                iterator.remove();
            }
        }
    }

    private void verifyHashMapsAreNotBlank(Process p) {
        if (!readyQueueWaitTimes.containsKey(p.pid)) {
            readyQueueWaitTimes.put(p.pid,0);
        }

        if (!memoryQueueWaitTimes.containsKey(p.pid)) {
            memoryQueueWaitTimes.put(p.pid,0);
        }
    }

    private Process dispatchNextProcess(int currentTime) {
        runningProcess = algo.selectNextProcess(readyQueue);
        if (runningProcess != null) {
            quantumLeft = TIME_QUANTUM;
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

    public Map<Integer,Integer> getReadyQueueWaitTimes() {
        return readyQueueWaitTimes;
    }
    public Map<Integer,Integer> getMemoryQueueWaitTimes() {
        return memoryQueueWaitTimes;
    }


}