package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class Return extends Operation {

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "return";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        // TODO Auto-generated method stub
    }
}