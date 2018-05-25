package yal2jvm.ir.operations;

import yal2jvm.common.StackSizeCounter;
import yal2jvm.ir.Statement;
import yal2jvm.scope.VariableDesc;

public abstract class Operation {

    protected Statement statement;
    protected LowIrNode container;

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public void setContainer(LowIrNode container) {
        this.container = container;
    }

    public abstract void optimize();

    public abstract void calculateStackSize(StackSizeCounter stackSizeCounter);

    public VariableDesc getDesc() {
        return null;
    }

    @Override
    public abstract String toString();

    public LowIrNode getContainer() {
        return container;
    }

    public Statement getStatement() {
        return statement;
    }

}
