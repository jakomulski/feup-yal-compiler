package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;

public class INeg extends Operation {
    @Override
    public void optimize() {
        LowIrNode child = container.getChildren().get(0);
        Operation operation = child.getOperation();
        if (IPush.class.equals(operation.getClass())) {
            performOptimize(Integer.valueOf(IPush.class.cast(operation).value));
        }
    }

    private void performOptimize(int num) {
        container.clearChildren();
        container.setOperation(new IPush(String.valueOf(-num)));
        container.getOperation().setContainer(container);
    }

    @Override
    public String toString() {
        return "ineg";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        // TODO Auto-generated method stub
    }
}