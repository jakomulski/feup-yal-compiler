package yal2jvm.dataflow.algorithms.leftedge;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Segment {

    private final Set<LiveRange> liveRanges = new HashSet<>();
    private final Set<String> names = new HashSet<>();

    private Integer registerNum = null;

    public void forEach(Consumer<? super LiveRange> action) {
        liveRanges.forEach(action);
    }

    public boolean contains(String name) {
        return names.contains(name);
    }

    public Integer getRegisterNum() {
        return registerNum;
    }

    public void add(LiveRange element) {
        names.add(element.getName());
        liveRanges.add(element);
    }

    public void setRegister(Integer registerNum) {
        this.registerNum = registerNum;
    }

    public Set<LiveRange> getLiveRanges() {
        return liveRanges;
    }

    public boolean isEmpty() {
        return liveRanges.isEmpty();
    }

    public boolean doNotOverlap(LiveRange first) {
        for (LiveRange second : liveRanges)
            if (first.getStart() >= second.getStart() && first.getStart() <= second.getEnd()
                    || first.getEnd() >= second.getStart() && first.getEnd() <= second.getEnd())
                return false;
        return true;
    }

}
