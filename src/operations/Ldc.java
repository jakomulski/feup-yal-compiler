package operations;

import custom.StackSizeCounter;

public class Ldc extends Operation {

    private final String tokenValue;

    public Ldc(String tokenValue) {
        this.tokenValue = tokenValue;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "ldc " + tokenValue;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();

    }
}