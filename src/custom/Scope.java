package custom;

import java.util.HashMap;

import yal2jvm.ASTFunction;
import yal2jvm.SimpleNode;

public class Scope {
	
	private final HashMap<String, ASTFunction> functions; 
	private final HashMap<String, SimpleNode> variables; 
	
	
	public Scope(){
		functions = new HashMap<>();
		variables = new HashMap<>();
	}
	
	private Scope(Scope scope){
		functions = new HashMap<>(scope.functions);
		variables = new HashMap<>(scope.variables);
	}
	
	
	public HashMap<String, ASTFunction> functions() {
		return functions;
	}
	public HashMap<String, SimpleNode> variables() {
		return variables;
	}

	public Scope copy(){
		return new Scope(this);
	}
}
