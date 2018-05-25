package yal2jvm.ir.operations;

import java.util.ArrayList;
import java.util.List;

import yal2jvm.ir.Statement;

public class LowIrNode {
    private Operation operation;

    private List<LowIrNode> children = new ArrayList<>();

    private Statement statement;

    public void clearChildren() {
        this.children.clear();
    }

    public LowIrNode(Operation operation, Statement statement) {
        this.statement = statement;
        this.operation = operation;
        this.operation.container = this;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<LowIrNode> getChildren() {
        return children;
    }

    public void addChild(LowIrNode operation) {
        children.add(operation);
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
        operation.container = this;
    }

    @Override
    public String toString() {
        return operation.toString();
    }

    public Statement getStatement() {
        return statement;

    }

}
