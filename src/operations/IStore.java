package operations;

import custom.StackSizeCounter;
import scope.VariableDesc;

public class IStore extends Operation {

    private final VariableDesc varDesc;

    public IStore(VariableDesc varDesc) {
        this.varDesc = varDesc;
    }

    private boolean isNumberNode(Operation op) {
        return IPush.class.equals(op.getClass());
    }

    @Override
    public void optimize() {
        LowIrNode child = container.getChildren().get(0);
        Operation operation = child.getOperation();
        if (Operator.class.equals(operation.getClass())) {
            LowIrNode firstChild = child.getChildren().get(0);
            LowIrNode secondChild = child.getChildren().get(1);

            if (ILoad.class.equals(firstChild.getOperation().getClass()) && isNumberNode(secondChild.getOperation())) {
                optimize((ILoad) firstChild.getOperation(), secondChild.getOperation(),
                        Operator.class.cast(operation).value);
            } else if (ILoad.class.equals(secondChild.getOperation().getClass())
                    && isNumberNode(firstChild.getOperation())) {
                optimize(firstChild.getOperation(), (ILoad) secondChild.getOperation(),
                        Operator.class.cast(operation).value);
            }
        }
    }

    public void optimize(ILoad iload, Operation numOperation, String operator) {
        if (iload.desc.equals(this.varDesc)) {
            switch (operator) {
            case "+":
                container.clearChildren();
                container.setOperation(new IInc(this.varDesc, getNumNodeValue(numOperation)));
                break;
            case "-":
                container.clearChildren();
                container.setOperation(new IInc(this.varDesc, getNumNodeValue(numOperation)));
                break;
            }
        }
    }

    public void optimize(Operation numOperation, ILoad iload, String operator) {
        if (iload.desc.equals(this.varDesc)) {
            switch (operator) {
            case "+":
                container.clearChildren();
                container.setOperation(new IInc(this.varDesc, getNumNodeValue(numOperation)));
                break;
            case "-":
                // container.clearChildren();
                // container.setOperation(new IInc(this.varDesc,
                // String.valueOf(-Integer.parseInt(biPush.value))));
                break;
            }
        }
    }

    private String getNumNodeValue(Operation operation) {
        return IPush.class.cast(operation).value;
    }

    @Override
    public VariableDesc getDesc() {
        return varDesc;
    }

    @Override
    public String toString() {
        return "istore " + varDesc.getName();
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
    }
}