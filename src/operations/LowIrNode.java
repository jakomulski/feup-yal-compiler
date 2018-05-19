package operations;

import java.util.ArrayList;
import java.util.List;

public class LowIrNode {
    private Operation operation;

    private List<LowIrNode> children = new ArrayList<>();

    protected void clearChildren() {
        this.children.clear();
    }

    public LowIrNode(Operation operation) {
        this.operation = operation;
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
    }

    @Override
    public String toString() {
        return operation.toString();
    }

}
