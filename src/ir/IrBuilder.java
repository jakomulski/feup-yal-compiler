package ir;


import java.util.ArrayList;
import java.util.List;

import scope.FunctionDesc;
import scope.Scope;
import yal2jvm.SimpleNode;

public class IrBuilder {
	
	private final FunctionDesc functionDesc;
	private List<Statement> statements = new ArrayList<>();
	
	public IrBuilder(FunctionDesc desc){
		this.functionDesc = desc;
	}
	
	public void addStatement(SimpleNode node, Scope scope){
		statements.add(new Statement(node, scope));
	}

	public void build() {
		System.out.println(functionDesc.getName());
		statements.forEach(statement ->{
			System.out.println(statement.getNode().toString());
		});		
	}
	
}
