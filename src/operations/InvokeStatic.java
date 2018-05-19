package operations;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import custom.StackSizeCounter;
import scope.FunctionDesc;
import scope.VariableType;

public class InvokeStatic extends Operation {
    final private Supplier<String> value;
    final private Consumer<StackSizeCounter> calculateStackSize;

    public InvokeStatic(String module, String name, List<String> args) {
        value = () -> module + "/" + name + "(" + String.join("", args) + ")" + statement.typeAsJasmin();
        calculateStackSize = (stCounter) -> {
            args.forEach(e -> stCounter.pop());
            if (statement.getType() != null && (statement.getType().equals(VariableType.SCALAR)
                    || statement.getType().equals(VariableType.ANY) || statement.getType().equals(VariableType.ARRAY)))
                stCounter.push();
        };
    }

    public InvokeStatic(String value, FunctionDesc fnDesc) {
        this.value = () -> value + "/" + fnDesc.asJasmin();
        calculateStackSize = (stCounter) -> {
            fnDesc.getArumentsTypes().forEach(e -> stCounter.pop());
            VariableType returnType = fnDesc.getReturnType();
            if (returnType.equals(VariableType.SCALAR) || returnType.equals(VariableType.ARRAY))
                stCounter.push();
        };
    }

    @Override
    public void optimize() {
        // super.container;

    }

    @Override
    public String toString() {
        return "invokestatic " + value.get();
    }

    @Override
    public void calculateStackSize(StackSizeCounter stackSizeCounter) {
        if (statement != null)
            calculateStackSize.accept(stackSizeCounter);
    }
}