package operations;

import custom.StackSizeCounter;

public class BiPush extends Operation {

    final String value;

    public BiPush(String value) {
        this.value = value;
    }

    @Override
    public void optimize() {
        int integerValue = Integer.parseInt(value);
        if (integerValue >= 0 && integerValue <= 5) {
            this.container.setOperation(new IConst(value));
        }
        this.container.getOperation().setContainer(container);
    }

    @Override
    public String toString() {
        return "bipush " + value;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }
}