package operations;

import custom.StackSizeCounter;
import scope.VariableDesc;

public class ILoad extends Operation {
    final VariableDesc desc;

    public ILoad(VariableDesc desc) {
        this.desc = desc;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        try {
            if (Integer.parseInt(desc.getName()) <= 3)
                return "iload_" + desc.getName();
        } catch (NumberFormatException e) {
            //
        }
        return "iload " + desc.getName();
    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }

}
