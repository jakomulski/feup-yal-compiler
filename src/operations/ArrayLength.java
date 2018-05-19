package operations;

import custom.StackSizeCounter;

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