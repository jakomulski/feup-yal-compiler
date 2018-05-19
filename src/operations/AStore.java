package operations;

import custom.StackSizeCounter;
import scope.VariableDesc;

public class AStore extends Operation {

    final private VariableDesc desc;

    public AStore(VariableDesc desc) {
        this.desc = desc;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "astore " + desc.getName();
    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
    }
}
