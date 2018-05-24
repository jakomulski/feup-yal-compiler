package operations;

import custom.StackSizeCounter;

public class IPush extends Operation {

    final int iValue;
    final String value;

    public IPush(String value) {
        this.value = value;
        this.iValue = Integer.parseInt(value);
    }

    @Override
    public void optimize() {

    }

    public int getIValue() {
        return iValue;
    }

    @Override
    public String toString() {
        if (iValue == -1)
            return "iconst_m1";
        else if (iValue >= 0 && iValue <= 5)
            return "iconst_" + value;
        else if (iValue >= -128 && iValue <= 127)
            return "bipush " + value;
        else if (iValue >= -32768 && iValue <= 32767)
            return "sipush " + value;
        return "ldc " + value;
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.push();
    }
}