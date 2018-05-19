package operations;

import custom.StackSizeCounter;

public class IConstM1 extends Operation {

    public IConstM1() {
    }

    @Override
    public void optimize() {

    }

    @Override
    public String toString() {
        return "iconst_m1";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }
}