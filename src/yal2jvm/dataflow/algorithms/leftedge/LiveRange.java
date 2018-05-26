package yal2jvm.dataflow.algorithms.leftedge;

class LiveRange {
    private final Integer start;
    private Integer end;
    private final String name;

    LiveRange(String name, Integer start) {
        this.start = start;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
