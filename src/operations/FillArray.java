package operations;

import custom.StackSizeCounter;
import scope.VariableDesc;

public class FillArray extends Operation {

    final VariableDesc arrayDesc;

    public FillArray(VariableDesc arrayDesc) {
        this.arrayDesc = arrayDesc;
    }

    @Override
    public void optimize() {
    }

    @Override
    public String toString() {
        return "invokestatic " + this.statement.scope.getModuleName() + "/&fill([II)V";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }
}