package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.scope.VariableDesc;

public class ALoad extends Operation {

    private final VariableDesc desc;

    public ALoad(VariableDesc desc) {
        this.desc = desc;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "aload " + desc.getName();
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();

    }
}
