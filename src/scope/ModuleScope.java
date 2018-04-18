package scope;

import java.util.Map;

public class ModuleScope extends Scope {
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
