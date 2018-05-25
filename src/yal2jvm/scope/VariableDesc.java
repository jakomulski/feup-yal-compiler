package yal2jvm.scope;

import yal2jvm.ir.operations.Operation;

public interface VariableDesc {
    VariableDesc initialize();

    String getValue();

    void setValue(String value);

    boolean isInitialized();

    boolean isField();

    boolean is(VariableType type);

    VariableType getType();

    boolean isUsed();

    void use();

    Operation getOperation();

    void setOperation(Operation operation);

    VariableDesc setType(VariableType type);

    String getName();

    void setName(String name);

    void fill();

    boolean isFill();

    void setUsed(boolean used);

}
