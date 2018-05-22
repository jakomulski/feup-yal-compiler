package operations;

import custom.StackSizeCounter;
import scope.VariableDesc;

public class ALoad extends Operation {

    private final VariableDesc desc;

    public ALoad(VariableDesc desc) {
        this.desc = desc;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "aload_" + desc.getName();
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();

    }
}
