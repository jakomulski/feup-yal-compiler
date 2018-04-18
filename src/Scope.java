import java.util.Map;

public abstract class Scope {
	Scope parent;
	protected Map<String, VariableDesc> variables;
	public Scope(Scope parent){
		this.parent = parent;
	}
	
	public VariableDesc getVariable(String name){
		if(this.variables.containsKey(name))
			return this.variables.get(name);
		if(parent != null)
			return parent.getVariable(name);
		return null;
	};
	
	public void addVariable(String name, VariableDesc desc){
		this.variables.put(name, desc);
	}
	
	
	public abstract FunctionDesc getFunction(String name);
}


class ModuleScope extends Scope {
	private Map<String, FunctionDesc> functions;

	public ModuleScope() {
		super(null);
	}
	
	public void addFunction(String name, FunctionDesc desc){
		this.functions.put(name, desc);
	}
	
	@Override
	public FunctionDesc getFunction(String name) {
		return functions.get(name);
	}
	
}

class FunctionScope extends Scope {
	public FunctionScope(ModuleScope parent) {
		super(parent);
	}

	@Override
	public FunctionDesc getFunction(String name) {
		return parent.getFunction(name);
	}
}