package ir;

import custom.NameGenerator;
import custom.StackSizeCounter;
import operations.AddOperation;
import operations.LowIrNode;
import operations.Operation;
import scope.Scope;
import scope.VariableDesc;
import scope.VariableType;
import yal2jvm.SimpleNode;

public class Statement implements AddOperation {
    public LowIrNode root;
    private SimpleNode node = null;
    private Statement ref = null;
    public Scope scope;
    private String name;
    private VariableDesc type;
    private StackSizeCounter stackSizeCounter;

    public Statement(SimpleNode node) {
        this.node = node;
    }

    public Statement(SimpleNode node, Scope scope, StackSizeCounter stackSizeCounter) {
        this.node = node;
        this.scope = scope;
        this.stackSizeCounter = stackSizeCounter;
    }

    public Statement() {
        this.node = new SimpleNode(-1);
        this.name = NameGenerator.INSTANCE.getName();
    }

    public String getName() {
        return name;
    }

    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        calculateStackSizeRec(this.root, stackSizeCounter);
    }

    private void calculateStackSizeRec(LowIrNode node, StackSizeCounter stackSizeCounter) {
        if (node == null)
            return;

        node.getChildren().forEach(ch -> calculateStackSizeRec(ch, stackSizeCounter));
        node.getOperation().calculateStackSize(stackSizeCounter);

    }

    public void setName(String name) {
        this.name = name;
    }

    public Statement getRef() {
        return ref;
    }

    public Statement setRef(Statement ref) {
        this.ref = ref;
        return this;
    }

    public SimpleNode getNode() {
        return node;
    }

    public AddOperation add(Operation operation) {
        this.root = new LowIrNode(operation);
        operation.setContainer(this.root);
        operation.setStatement(this);
        return addNode(root, this);
        // .add(operation);
    }

    public void setType(VariableDesc type) {
        this.type = type;
    }

    public VariableType getType() {
        if (type == null)
            return null;
        return this.type.getType();
    }

    public String typeAsJasmin() {
        if (type == null)
            return "V";
        if (type.getType().equals(VariableType.SCALAR) || type.getType().equals(VariableType.ANY))
            return "I";
        else if (type.getType().equals(VariableType.ARRAY))
            return "[I";
        else
            return "V";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // print("", sb, root);
        toStringRec("  ", sb, root);
        return sb.toString();
    }

    private void toStringRec(String prefix, StringBuilder sb, LowIrNode node) {
        if (node == null)
            return;

        node.getChildren().forEach(ch -> toStringRec(prefix + " ", sb, ch));

        String append = node.toString() + System.lineSeparator();
        sb.append(prefix + append);
    }

    private void print(String prefix, StringBuilder sb, LowIrNode node) {
        if (node == null)
            return;
        sb.append(prefix + node.toString() + System.lineSeparator());
        node.getChildren().forEach(ch -> print(prefix + " ", sb, ch));
    }

    public void optimize() {
        optimize(root);
    }

    private void optimize(LowIrNode node) {
        if (node == null)
            return;
        node.getChildren().forEach(ch -> optimize(ch));
        node.getOperation().optimize();
    }
}
