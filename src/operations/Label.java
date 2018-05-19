package operations;

import custom.StackSizeCounter;
import ir.Statement;

public class Label extends Operation {

    final private Statement statement;

    public Label(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return statement.getName() + ":";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        // TODO Auto-generated method stub

    }
}
