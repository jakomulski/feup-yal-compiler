package ir;

import java.util.ArrayList;
import java.util.List;

import scope.Scope;
import yal2jvm.SimpleNode;

public class CodeBuilder {
	
	private final SimpleNode moduleNode;
	private final Scope rootScope;
	
	
	private final List<IrBuilder> functions = new ArrayList<>(); 
	
	public CodeBuilder(SimpleNode node, Scope scope){
		this.moduleNode = node;
		this.rootScope = scope;
	}

	public void addIrBuilder(IrBuilder irBuilder){
		functions.add(irBuilder);
	}
	
	public void build() {
		String className = moduleNode.getTokenValue();
		System.out.println(".class public "+ className + ".super java/lang/Object");
		
		functions.forEach(f->f.build());
		
		
	};
}
