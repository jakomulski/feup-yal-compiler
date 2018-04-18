package scope;
import java.util.HashMap;
import java.util.Map;

public abstract class Scope {
	Scope parent;
	protected Map<String, VariableDesc> variables = new HashMap<>();
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
	
	public Scope getParent(){
		return parent;
	}
	
	public abstract FunctionDesc getFunction(String name);

	@Override
	public String toString() {
		if(parent != null){
			return "variables = " + variables + "\n" + parent.toString();
		}
		return "variables = " + variables;
	}
}



