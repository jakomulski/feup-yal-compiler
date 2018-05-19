package operations;

import custom.StackSizeCounter;

public class IfIcmp extends Operation {

    final private String condition;

    public IfIcmp(String condition) {
        this.condition = condition;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return convert(condition) + " " + statement.getRef().getName();
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
            return "if_icmpge";
        case "<=":
            return "if_icmpgt";
        }
        return "";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.pop();

    }
}