package scope;

import java.util.HashMap;
import java.util.Map;

public class ModuleScope extends Scope {
    private Map<String, FunctionDesc> functions = new HashMap<>();
    private String moduleName = "";

    public ModuleScope() {
        super(null);
    }

    public void setModuleName(String name) {
        this.moduleName = name;
    }

    public void addFunction(String name, FunctionDesc desc) {

        this.functions.put(name, desc);
    }

    @Override
    public FunctionDesc getFunction(String name) {
        return functions.get(name);
    }

    @Override
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    @Override
    public String toString() {
        return "functions = " + functions + "\n" + super.toString();
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

}
