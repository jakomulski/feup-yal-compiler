package yal2jvm.dataflow.algorithms.graphcoloring;

import java.util.Set;
import java.util.function.Consumer;

public class Segment {

    private final Set<String> names;
    private Integer registerNum = null;

    public Segment(Set<String> names) {
        this.names = names;
    }

    public Integer getRegisterNum() {
        return registerNum;
    }

    public void setRegister(Integer registerNum) {
        this.registerNum = registerNum;
    }

    public boolean contains(String name) {
        return names.contains(name);
    }

    public void forEach(Consumer<String> action) {
        names.forEach(action);
    }
}
