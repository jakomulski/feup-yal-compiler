package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class IReturn extends Operation {

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "ireturn";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();

    }
}