package minios;

import java.io.IOException;
import java.util.*;

public class Simulator {
    private final Kernel kernel;
    private final List<Process> incomingProcesses;
    private int clock = 0;

    public Simulator(Kernel kernel, List<Process> processes) {
        this.kernel = kernel;
        this.incomingProcesses = processes;
    }

    public void run() {
        // Continue the simulation as long as:
        // 1. there are more processes to come; or
        // 2. one or more existing processes have not completed yet
        while (!incomingProcesses.isEmpty() || !kernel.isIdle()) {
            // Step 1: Check if any processes should arrive now
            Iterator<Process> it = incomingProcesses.iterator();
            while (it.hasNext()) {
                Process p = it.next();
                if (p.arrivalTime == clock) {
                    System.out.println("[Tick " + clock + "] Process " + p.pid + " arrives.");
                    kernel.admitProcess(p);
                    it.remove();
                }
            }

            // Step 2: perform tasks that need to happen during this clock tick
            kernel.onClockTick(clock);

            // Step 3: Advance the simulation clock
            clock++;
        }

    }

    public static void main(String[] args) throws Exception{
        int memoryMode = getMemoryMode();
        int quantum = 0;
        System.out.println("FCFS");
        pause();
        SchedulingAlgo FCFSAlgo = new FCFS();
        startSim(FCFSAlgo, quantum, memoryMode);
        System.out.println("SJF");
        pause();
        SchedulingAlgo SJFAlgo = new SJF();
        startSim(SJFAlgo, quantum, memoryMode);
        System.out.println("RR2");
        pause();
        quantum = 2;
        SchedulingAlgo RR2Algo = new RR2();
        startSim(RR2Algo, quantum, memoryMode);

    }

    private static void pause() {
        do {
            System.out.println("Press enter to continue to run the algorithm");
            if (!new Scanner(System.in).nextLine().equalsIgnoreCase("")) {
                System.out.println("That's not enter");
            } else {
                break;
            }
        } while(true);
    }

    private static int getMemoryMode() {
        int memoryMode;
        do {
            System.out.print("Enter memory mode\n0=Contiguous\n1=Paged\nSelection: ");
            memoryMode = Integer.parseInt(new Scanner(System.in).nextLine());
        } while (memoryMode != 0 && memoryMode != 1);
        return memoryMode;
    }

    private static void startSim(SchedulingAlgo Algo, int quantum, int memoryModeFlag) throws IOException {
        List<Process> universalWorkload = TraceParser.parseWorkload("workload.txt");
        Map<Integer, Integer> arrivalTimes = new HashMap<>();
        getArrivalTimes(universalWorkload, arrivalTimes);

        System.out.println("\n\nSimulation START!");
        Kernel Kernel = new Kernel(Algo, quantum, memoryModeFlag);
        Simulator Sim = new Simulator(Kernel, universalWorkload);Sim.run();
        System.out.println("Average wait time: " + getAverageWaitTime(arrivalTimes, Kernel.getStartTimes(), quantum, Kernel.getTimesPreempted()));
        System.out.println("Simulation END!\n\n");
    }


    private static void getArrivalTimes(List<Process> universalWorkload, Map<Integer, Integer> arrivalTimes) {
        for (Process process : universalWorkload) {
            arrivalTimes.put(process.pid, process.arrivalTime);
        }
    }

    private static double getAverageWaitTime(Map<Integer, Integer> arrivalTimes, Map<Integer, Integer> startTimes, int quantum, Map<Integer, Integer> timesPreempted) {
        double runningTotal = 0;
        System.out.println("Arrives: " + arrivalTimes);
        System.out.println("Starts: " + startTimes);
        System.out.println("Pre-empts: " + timesPreempted);
        for (int x = 1; x <= arrivalTimes.size();x++) {
            timesPreempted.putIfAbsent(x, 0);
            runningTotal += startTimes.get(x) - arrivalTimes.get(x) - (timesPreempted.get(x) * quantum);
        }
        return runningTotal / arrivalTimes.size();
    }

}

