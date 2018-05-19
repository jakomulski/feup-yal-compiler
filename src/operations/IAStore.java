package operations;

import custom.StackSizeCounter;

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