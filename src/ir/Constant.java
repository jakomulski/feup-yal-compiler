package ir;

import operations.Operation;

public class Constant {
    public static final Constant VARIABLE = new Constant(null, null);

    private Statement statement;
    private Operation operation;

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