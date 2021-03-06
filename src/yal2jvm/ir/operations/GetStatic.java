package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.scope.VariableDesc;
import yal2jvm.scope.VariableType;

public class GetStatic extends Operation {

    final private VariableDesc varDesc;
    final private String module;

    public GetStatic(String module, VariableDesc varDesc) {
        this.varDesc = varDesc;
        this.module = module;
    }

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "getstatic " + convert();
    }

    private String convert() {
        if (varDesc.is(VariableType.SCALAR))
            return module + "/" + varDesc.getName() + " I";
        else if (varDesc.is(VariableType.ARRAY))
            return module + "/" + varDesc.getName() + " [I";
        return "error";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();

    }
}