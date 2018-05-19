package operations;

import custom.StackSizeCounter;

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