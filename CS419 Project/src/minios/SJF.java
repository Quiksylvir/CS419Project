package minios;

import java.util.List;

public class SJF implements  SchedulingAlgo{


    @Override
    public void addProcess(List<Process> readyQueue, Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process selectNextProcess(List<Process> readyQueue) {
        if (readyQueue.isEmpty()) {
            return null;
        } else {
            if (readyQueue.size() == 1) {
                return readyQueue.removeFirst();
            } else {
                int tempSmallest = 0;
                for (int index = 0; index < readyQueue.size() - 1; index++) {
                    if (getDuration(index, readyQueue) < getDuration(tempSmallest, readyQueue)) {
                        tempSmallest = index;
                    }
                }
                return readyQueue.remove(tempSmallest);
            }

        }
    }

    public int getDuration(int index, List<Process> readyQueue) {
        return readyQueue.get(index).getCurrentInstruction().duration;
    }
}
