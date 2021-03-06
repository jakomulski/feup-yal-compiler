package yal2jvm.scope;

import yal2jvm.ir.operations.Operation;

public enum VariableDescFactory {
    INSTANCE;

    public VariableDesc createLocalVariable(VariableType type, boolean initialized) {
        return new VariableDescImpl(type, initialized);
    }

    public VariableDesc createField(VariableType type, boolean initialized) {
        VariableDescImpl varDesc = new VariableDescImpl(type, initialized);
        varDesc.isField = true;
        return varDesc;
    }

    public VariableDesc createCopy(VariableDesc variableDesc) {
        return new VariableDescImpl(variableDesc);
    }

    class Wrapper<T> {
        private T obj;

        Wrapper(T obj) {
            this.obj = obj;
        }

        public T get() {
            return obj;
        }

        public void set(T obj) {
            this.obj = obj;
        }

    }

    class VariableDescImpl implements VariableDesc {

        private Wrapper<String> name = new Wrapper<>("");

        private boolean initialized;
        private VariableType type;
        private boolean isField = false;
        private boolean fill = false;
        private String value;
        private boolean isUsed = true;
        private Operation operation = null;

        private boolean isReturnValue = false;

        public VariableDescImpl(VariableDesc variableDesc) {
            name = VariableDescImpl.class.cast(variableDesc).name;
            type = variableDesc.getType();
            initialized = variableDesc.isInitialized();
            value = variableDesc.getValue();
            isField = variableDesc.isField();
        }

        public VariableDescImpl(VariableType type, boolean initialized) {
            this.type = type;
            this.initialized = initialized;
        }

        public VariableDescImpl initialize() {
            this.initialized = true;
            return this;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isField() {
            return isField;
        }

        public boolean isInitialized() {
            return initialized;
        }

        public boolean is(VariableType type) {
            return this.type.equals(type);
        }

        public VariableType getType() {
            return type;
        }

        public VariableDescImpl setType(VariableType type) {
            this.type = type;
            return this;
        }

        @Override
        public String toString() {
            return "VariableDesc [ initialized=" + initialized + ", type=" + type + "]";
        }

        public void setName(String name) {
            this.name.set(name);
        }

        @Override
        public String getName() {
            return this.name.get();
        }

        @Override
        public void fill() {
            fill = true;
        }

        @Override
        public boolean isFill() {
            return fill;
        }

        @Override
        public boolean isUsed() {
            return isUsed;
        }

        @Override
        public void use() {
            isUsed = true;
        }

        @Override
        public Operation getOperation() {
            if (operation != null && operation.getDesc() != null && operation.getDesc().isField())
                return null;
            return operation;
        }

        @Override
        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        @Override
        public void setUsed(boolean used) {
            this.isUsed = used;

        }

        @Override
        public void markAsReturnValue() {
            this.isReturnValue = true;

        }

        @Override
        public boolean isReturnValue() {
            return isReturnValue;
        }

    }

}
