package yal2jvm.dataflow.algorithms;

import java.util.HashSet;
import java.util.Set;

public class Segment {

    private final Set<LiveRange> liveRanges = new HashSet<>();
    private Integer registerNum = null;

    public Integer getRegisterNum() {
        return registerNum;
    }

    public void setRegisterNum(Integer registerNum) {
        this.registerNum = registerNum;
    }

    public Set<LiveRange> getLiveRanges() {
        return liveRanges;
    }

}
