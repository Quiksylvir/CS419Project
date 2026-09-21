package minios;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        int quantum = 0;
        SchedulingAlgo FCFSAlgo = new FCFS();
        startSim(FCFSAlgo, quantum);

        SchedulingAlgo SJFAlgo = new SJF();
        startSim(SJFAlgo, quantum);

        quantum = 2;
        SchedulingAlgo RR2Algo = new RR2();
        startSim(RR2Algo, quantum);

    }

    private static void startSim(SchedulingAlgo Algo, int quantum) throws IOException {
        List<Process> universalWorkload = TraceParser.parseWorkload("workload.txt");
        Map<Integer, Integer> arrivalTimes = new HashMap<>();
        getArrivalTimes(universalWorkload, arrivalTimes);

        System.out.println("\n\nSimulation START!");
        Kernel Kernel = new Kernel(Algo, quantum);
        Simulator Sim = new Simulator(Kernel, universalWorkload);Sim.run();
        System.out.println("Average wait time: " + getAverageWaitTime(arrivalTimes, Kernel.getStartTimes(), quantum, Kernel.getTimesPreempted()));
        System.out.println("Simulation END!\n\n");
    }


    private static void getArrivalTimes(List<Process> universalWorkload, Map<Integer, Integer> arrivalTimes) {
        for (int x = 0; x < universalWorkload.size(); x++) {
            arrivalTimes.put(universalWorkload.get(x).pid, universalWorkload.get(x).arrivalTime);
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

