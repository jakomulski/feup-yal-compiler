package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class NewArray extends Operation {

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "newarray int";
        // newarray int
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.push();

    }
}