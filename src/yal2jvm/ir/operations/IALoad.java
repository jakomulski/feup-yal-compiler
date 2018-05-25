package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class IALoad extends Operation {
    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "iaload";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.pop();
        stackSizeCounter.push();
    }
}
