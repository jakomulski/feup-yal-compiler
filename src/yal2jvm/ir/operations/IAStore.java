package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class IAStore extends Operation {
    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "iastore";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.pop();
        stackSizeCounter.pop();
    }
}