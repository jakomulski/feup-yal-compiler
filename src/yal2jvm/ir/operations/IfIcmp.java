package yal2jvm.ir.operations;

import java.util.List;
import java.util.Optional;

import yal2jvm.common.StackSizeCounter;

public class IfIcmp extends Operation {

    final private String condition;
    private boolean hasZero = false;

    public IfIcmp(String condition) {
        this.condition = condition;
    }

    @Override
    public void optimize() {
        List<LowIrNode> children = container.getChildren();
        Operation secondChild = children.get(1).getOperation();
        if (secondChild.getClass().equals(IPush.class) && IPush.class.cast(secondChild).iValue == 0) {
            children.remove(1);
            this.hasZero = true;
        }

    }

    @Override
    public String toString() {
        String converted = hasZero ? convertForZero(condition) : convert(condition);
        return converted + " " + statement.getRef().getName();
    }

    private String convertForZero(String condition) { // return opposite
        switch (condition) {
        case "==":
            return "ifne";
        case "!=":
            return "ifeq";
        case ">":
            return "ifle";
        case "<":
            return "ifge";
        case ">=":
            return "iflt";
        case "<=":
            return "ifgt";
        }
        return "";
    }

    private String convert(String condition) { // return opposite
        switch (condition) {
        case "==":
            return "if_icmpne";
        case "!=":
            return "if_icmpeq";
        case ">":
            return "if_icmple";
        case "<":
            return "if_icmpge";
        case ">=":
            return "if_icmplt";
        case "<=":
            return "if_icmpgt";
        }
        return "";
    }

    public Optional<Boolean> checkCondition() {
        List<LowIrNode> children = this.container.getChildren();
        Operation firstCondition = children.get(0).getOperation();
        if (children.size() == 1) {
            if (firstCondition.getClass().equals(IPush.class)) {
                boolean condition = checkCondition(IPush.class.cast(firstCondition).getIValue(), 0);
                return Optional.of(condition);
            }
        } else {
            Operation secondCondition = this.container.getChildren().get(1).getOperation();
            if (firstCondition.getClass().equals(IPush.class) && secondCondition.getClass().equals(IPush.class)) {
                boolean condition = checkCondition(IPush.class.cast(firstCondition).getIValue(),
                        IPush.class.cast(secondCondition).getIValue());
                return Optional.of(condition);
            }
        }

        return Optional.empty();
    }

    private boolean checkCondition(int first, int second) {

        switch (condition) {
        case "==":
            return first == second;
        case "!=":
            return first != second;
        case ">":
            return first > second;
        case "<":
            return first < second;
        case ">=":
            return first >= second;
        case "<=":
            return first <= second;
        }
        return false;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        if (!hasZero)
            stackSizeCounter.pop();
        stackSizeCounter.pop();

    }
}