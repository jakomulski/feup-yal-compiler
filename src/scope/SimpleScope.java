package scope;

public class SimpleScope extends Scope {

    public SimpleScope(Scope parent) {
        super(parent);
    }

    @Override
    public FunctionDesc getFunction(String name) {
        return parent.getFunction(name);
    }

    @Override
    public boolean hasFunction(String name) {
        return parent.hasFunction(name);
    }

    @Override
    public String getModuleName() {
        return parent.getModuleName();
    }
}
