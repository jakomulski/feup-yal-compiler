package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class Pop extends Operation {

    public Pop() {
    }

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "pop";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
    }
}
