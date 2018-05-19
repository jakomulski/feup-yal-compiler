package operations;

import custom.StackSizeCounter;

public class Operator extends Operation {

    final String value;

    public Operator(String value) {
        this.value = value;
    }

    @Override
    public void optimize() {

        LowIrNode firstChild = container.getChildren().get(0);
        LowIrNode secondChild = container.getChildren().get(1);
        Class<?> firstClazz = firstChild.getOperation().getClass();
        Class<?> secondClazz = secondChild.getOperation().getClass();

        if (firstClazz.equals(BiPush.class) && secondClazz.equals(BiPush.class)) {
            optimizeBiPushBiPush(firstChild, secondChild);
        }

    }

    private void optimizeBiPushBiPush(LowIrNode firstChild, LowIrNode secondChild) {
        Integer firstNum = Integer.valueOf(BiPush.class.cast(firstChild.getOperation()).value);
        Integer secondNum = Integer.valueOf(BiPush.class.cast(secondChild.getOperation()).value);
        container.clearChildren();

        int num = 0;
        switch (value) {
        case "+":
            num = firstNum + secondNum;
            break;
        case "-":
            num = firstNum - secondNum;
            break;
        case "*":
            num = firstNum * secondNum;
            break;
        case "/":
            num = firstNum / secondNum;
            break;
        case ">>":
            num = firstNum >> secondNum;
            break;
        case "<<":
            num = firstNum << secondNum;
            break;
        case ">>>":
            num = firstNum >>> secondNum;
            break;
        case "&":
            num = firstNum & secondNum;
            break;
        case "|":
            num = firstNum | secondNum;
            break;
        case "^":
            num = firstNum ^ secondNum;
            break;
        }
        container.setOperation(new BiPush(String.valueOf(num)));
    }

    @Override
    public String toString() {
        return convert(value);
    }

    private String convert(String value) {
        switch (value) {
        case "+":
            return "iadd";
        case "-":
            return "isub";
        case "*":
            return "imul";
        case "/":
            return "idiv";
        case ">>":
            return "ishr";
        case "<<":
            return "ishl";
        case ">>>":
            return "iushr";
        case "&":
            return "iand";
        case "|":
            return "ior";
        case "^":
            return "ixor";
        }
        return "";
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        stackSizeCounter.pop();
        stackSizeCounter.pop();
        stackSizeCounter.push();

    }
}