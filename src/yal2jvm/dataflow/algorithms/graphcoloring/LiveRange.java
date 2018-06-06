package yal2jvm.dataflow.algorithms.graphcoloring;

import java.util.HashSet;
import java.util.Set;

class LiveRange {
    Set<Integer> liveRows = new HashSet<>();
    private final String name;

    LiveRange(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean doOverlap(LiveRange second) {
        for (Integer row : liveRows) {
            if (second.liveRows.contains(row))
                return true;
        }
        return false;
    }

    public LiveRange addIn(int num) {
        liveRows.add(num * 2);
        return this;
    }

    public LiveRange addOut(int num) {
        liveRows.add(num * 2 + 1);
        return this;
    }
}
