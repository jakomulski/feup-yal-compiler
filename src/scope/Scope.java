package scope;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;




public abstract class Scope {
	Scope parent;
	protected Map<String, VariableDesc> variables = new HashMap<>();
	public Scope(Scope parent){
		this.parent = parent;
	}
	
	public void mergeInitialized(BlockedSimpleScope scope, BlockedSimpleScope ... args){
		
		scope.blocked.forEach((k,v)->{
			boolean isInitialized = true;
			if(v.isInitialized()){
				for(BlockedSimpleScope sc : args)
					if(!sc.blocked.containsKey(k) || !sc.blocked.get(k).isInitialized())
						isInitialized = false;
			}
			else{
				isInitialized = false;
			}
			if(isInitialized)
				this.getVariable(k).initialize();
		});
		
	}
	
	public VariableDesc getVariable(String name){
		if(this.variables.containsKey(name))
			return this.variables.get(name);
		if(parent != null)
			return parent.getVariable(name);
		return null;
	};
	
	public boolean hasVariable(String name) {
		if(this.variables.containsKey(name))
			return true;
		if(parent != null)
			return parent.hasVariable(name);
		return false;
	};
	
	
	public void addVariable(String name, VariableDesc desc){
		this.variables.put(name, desc);
	}
	
	public Scope getParent(){
		return parent;
	}
	
	public abstract FunctionDesc getFunction(String name);
	public abstract boolean hasFunction(String name);

	@Override
	public String toString() {
		
		if(parent != null){
			return "variables = " + variables + "\n" + parent.toString();
		}
		return "variables = " + variables;
	}
}



