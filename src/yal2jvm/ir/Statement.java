package yal2jvm.ir;

import jjtree.SimpleNode;
import yal2jvm.common.NameGenerator;
import yal2jvm.common.StackSizeCounter;
import yal2jvm.ir.operations.AddOperation;
import yal2jvm.ir.operations.LowIrNode;
import yal2jvm.ir.operations.Operation;
import yal2jvm.scope.Scope;
import yal2jvm.scope.VariableDesc;
import yal2jvm.scope.VariableType;

public class Statement implements AddOperation {
    public LowIrNode root;
    private SimpleNode node = null;
    private Statement ref = null;
    public Scope scope;
    private String name;
    private VariableDesc type;
    private boolean cleared = false;
    private boolean clearBlock = false;
    private Statement endLabel;
    private Statement elseEndLabel;
    private Statement endLoopLabel;
    private Statement goToEndElseStatement;
    private Statement loopCondition;

    public Statement(SimpleNode node) {
        this.node = node;
    }

    public Statement(SimpleNode node, Scope scope) {
        this.node = node;
        this.scope = scope;
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
        this.root = new LowIrNode(operation, this);
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
        if (cleared)
            return "";
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

    public void clear() {
        if (clearBlock)
            return;
        this.cleared = true;

    }

    public boolean isCleared() {
        return cleared;
    }

    private void optimize(LowIrNode node) {
        if (node == null)
            return;
        node.getChildren().forEach(ch -> optimize(ch));
        node.getOperation().optimize();
    }

    public void blockClear() {
        this.clearBlock = true;

    }

    public boolean isIf() {
        return this.endLabel != null;
    }

    public Statement getIfEndLabel() {
        return this.endLabel;
    }

    public void addIfEndLabel(Statement endLabel) {
        this.endLabel = endLabel;

    }

    public void addElseEndLabel(Statement elseEndLabel) {
        this.elseEndLabel = elseEndLabel;
    }

    public boolean isIfElse() {
        return this.elseEndLabel != null;
    }

    public Statement getElseEndLabel() {
        return this.elseEndLabel;
    }

    public void addLoopEndLabel(Statement endLoopLabel) {
        this.endLoopLabel = endLoopLabel;
    }

    public boolean isLoop() {
        return this.endLoopLabel != null;
    }

    public Statement getLoopEndLabel() {
        return this.endLoopLabel;
    }

    public void addGoToEndElseStatement(Statement goToEndElse) {
        this.goToEndElseStatement = goToEndElse;
    }

    public Statement getGoToEndElseStatement() {
        return this.goToEndElseStatement;
    }

    public void addLoopCondition(Statement goToLoop) {
        this.loopCondition = goToLoop;
    }

    public Statement getLoopCondition() {
        return this.loopCondition;
    }
}
