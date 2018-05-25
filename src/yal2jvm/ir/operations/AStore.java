package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.scope.VariableDesc;

public class AStore extends Operation {

    final private VariableDesc desc;

    public AStore(VariableDesc desc) {
        this.desc = desc;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "astore " + desc.getName();
    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
    }
}
