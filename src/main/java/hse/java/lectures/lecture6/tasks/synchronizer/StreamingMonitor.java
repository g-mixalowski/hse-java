package hse.java.lectures.lecture6.tasks.synchronizer;

import java.util.*;

public class StreamingMonitor {

    private final List<Integer> orderedIds;
    private final int ticksPerWriter;
    private final Map<Integer, Integer> ticksDone = new HashMap<>();
    private int currentIndex = 0;
    private int totalDone = 0;
    private final int requiredTotal;

    public StreamingMonitor(List<StreamWriter> writers, int ticksPerWriter) {
        if (writers == null || writers.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (ticksPerWriter < 0) {
            throw new IllegalArgumentException();
        }
        this.ticksPerWriter = ticksPerWriter;

        TreeSet<Integer> sorted = new TreeSet<>();
        for (StreamWriter w : writers) {
            sorted.add(w.getId());
        }
        this.orderedIds = new ArrayList<>(sorted);
        for (Integer id : orderedIds) {
            ticksDone.put(id, 0);
        }
        this.requiredTotal = orderedIds.size() * ticksPerWriter;
    }

    public synchronized boolean awaitTurn(int id) throws InterruptedException {
        while (true) {
            if (totalDone >= requiredTotal) {
                return false;
            }
            int currentId = orderedIds.get(currentIndex);
            if (id == currentId && ticksDone.get(id) < ticksPerWriter) {
                return true;
            }
            wait();
        }
    }

    public synchronized void tickDone(int id) {
        if (totalDone >= requiredTotal) {
            notifyAll();
            return;
        }

        int done = ticksDone.get(id) + 1;
        ticksDone.put(id, done);
        totalDone++;

        if (totalDone >= requiredTotal) {
            notifyAll();
            return;
        }

        int n = orderedIds.size();
        for (int step = 1; step <= n; step++) {
            int idx = (currentIndex + step) % n;
            int nextId = orderedIds.get(idx);
            if (ticksDone.get(nextId) < ticksPerWriter) {
                currentIndex = idx;
                break;
            }
        }

        notifyAll();
    }

    public synchronized void awaitCompletion() throws InterruptedException {
        while (totalDone < requiredTotal) {
            wait();
        }
    }
}
