package operations;

import custom.StackSizeCounter;

public class IfIcmp extends Operation {

    final private String condition;
    final private boolean revert;

    public IfIcmp(String condition) {
        this.condition = condition;
        this.revert = true;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        String converted = revert ? revert(condition) : convert(condition);
        return converted + " " + statement.getRef().getName();
    }

    private String convert(String condition) { // return opposite
        switch (condition) {
        case "==":
            return "if_icmpeq";
        case "!=":
            return "if_icmpne";
        case ">":
            return "if_icmpgt";
        case "<":
            return "if_icmplt";
        case ">=":
            return "if_icmpge";
        case "<=":
            return "if_icmple";
        }
        return "";
    }

    private String revert(String condition) { // return opposite
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

    public boolean checkCondition(int first, int second) {
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
        stackSizeCounter.pop();
        stackSizeCounter.pop();

    }
}