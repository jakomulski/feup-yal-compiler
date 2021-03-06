package yal2jvm.optimization;

import yal2jvm.ir.Statement;
import yal2jvm.ir.operations.Operation;

public class Constant {

    private Statement statement;
    private Operation operation = null;

    public boolean isConstant() {
        return operation != null;
    }

    public Constant(Statement statement) {
        this.setStatement(statement);
        this.operation = null;
    }

    public Constant(Statement statement, Operation operation) {
        this.setStatement(statement);
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;

    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
