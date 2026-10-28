package minios;

import java.util.List;

public class RR2 implements SchedulingAlgo{
    @Override
    public void addProcess(List<Process> readyQueue, Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process selectNextProcess(List<Process> readyQueue) {
        if (readyQueue.isEmpty()) {
            return null;
        } else {
            return readyQueue.removeFirst();
        }
    }
}
