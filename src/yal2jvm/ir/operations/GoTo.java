package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.ir.Statement;

public class GoTo extends Operation {

    final private Statement ref;

    public GoTo(Statement ref) {
        this.ref = ref;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "goto " + ref.getName();
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        // TODO Auto-generated method stub

    }
}
