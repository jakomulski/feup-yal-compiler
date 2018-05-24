package operations;

import custom.StackSizeCounter;
import ir.Statement;
import scope.VariableDesc;

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
