package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class ArrayLength extends Operation {
    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "arraylength";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.push();

    }
}