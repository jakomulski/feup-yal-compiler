package operations;

import custom.StackSizeCounter;

public class INeg extends Operation {
    @Override
    public void optimize() {
        LowIrNode child = container.getChildren().get(0);
        Operation operation = child.getOperation();
        if (BiPush.class.equals(operation.getClass())) {
            performOptimize(Integer.valueOf(BiPush.class.cast(operation).value));
        } else if (IConst.class.equals(operation.getClass())) {
            performOptimize(Integer.valueOf(IConst.class.cast(operation).value));
        }
    }

    private void performOptimize(int num) {
        container.clearChildren();
        if (num == 1) {
            container.setOperation(new IConstM1());
        } else {
            container.setOperation(new BiPush(String.valueOf(-num)));
        }
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