package operations;

import custom.StackSizeCounter;

public class AReturn extends Operation {

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "areturn";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();

    }
}