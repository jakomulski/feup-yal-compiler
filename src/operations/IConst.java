package operations;

import custom.StackSizeCounter;

public class IConst extends Operation {

    final String value;

    public IConst(String value) {
        this.value = value;
    }

    @Override
    public void optimize() {
    }

    @Override
    public String toString() {
        return "iconst_" + value;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }
}