package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.scope.VariableDesc;

public class IInc extends Operation {

    final private VariableDesc desc;
    final private String value;

    public IInc(VariableDesc desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    @Override
    public void optimize() {
        // super.container;
    }

    @Override
    public String toString() {
        return "iinc " + desc.getName() + " " + value;
    }

    @Override
    public VariableDesc getDesc() {
        return desc;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        // TODO Auto-generated method stub

    }
}