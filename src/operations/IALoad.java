package operations;

import custom.StackSizeCounter;

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
