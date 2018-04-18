package semantic;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import scope.FunctionDesc;
import scope.FunctionScope;
import scope.ModuleScope;
import scope.Scope;
import scope.SimpleScope;
import scope.VariableDesc;
import scope.VariableType;
import yal2jvm.ASTFunction;
import yal2jvm.ASTModule;
import yal2jvm.ASTStatements;
import yal2jvm.Node;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvmTreeConstants;

public class SemanticAnalyzer {

	ASTModule module;

	ModuleScope rootScope;

	public SemanticAnalyzer(ASTModule module) {
		this.module = module;
		rootScope = new ModuleScope();
	}

	Queue<Runnable> toAnalyze = new LinkedList<>();

	public void analyze() {
		analyzeModule("", module);
		toAnalyze.forEach(r -> r.run());
		System.out.println(rootScope);
	}

	public void analyzeModule(String prefix, SimpleNode node) {
		if (node.getChildren() != null) {
			for (int i = 0; i < node.getChildren().length; ++i) {
				SimpleNode n = (SimpleNode) node.getChildren()[i];
				if (n != null) {
					if (n.is(Yal2jvmTreeConstants.JJTFUNCTION)) {
						analyzeFunction((ASTFunction) n);
					} else {
						analyzeDeclaration(n);
					}

				}
			}
		}
	}

	private void analyzeFunction(ASTFunction node) {
		FunctionDesc fnDesc = new FunctionDesc();
		rootScope.addFunction(node.getTokenValue(), fnDesc);
		SimpleScope functionScope = new SimpleScope(rootScope);
		SimpleNode returnNode = (SimpleNode) node.jjtGetChild(0);
		if (returnNode.jjtGetNumChildren() == 0) {
			fnDesc.setReturnType(VariableType.NULL);
		} else {
			returnNode = (SimpleNode) returnNode.jjtGetChild(0);
			if (returnNode.is(Yal2jvmTreeConstants.JJTARRAYVARIABLE)) {
				fnDesc.setReturnType(VariableType.ARRAY);
				functionScope.addVariable(returnNode.getTokenValue(), new VariableDesc(VariableType.ARRAY, false));
			} else if (returnNode.is(Yal2jvmTreeConstants.JJTSCALARVARIABLE)) {
				fnDesc.setReturnType(VariableType.SCALAR);
				functionScope.addVariable(returnNode.getTokenValue(), new VariableDesc(VariableType.SCALAR, false));
			}
		}
		SimpleNode parametersNode = (SimpleNode) node.jjtGetChild(1);
		if (parametersNode.getChildren() != null)
			for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
				if (parameter.is(Yal2jvmTreeConstants.JJTARRAYVARIABLE)) {
					fnDesc.addArumentType(VariableType.ARRAY);
					functionScope.addVariable(parameter.getTokenValue(), new VariableDesc(VariableType.ARRAY, true));
				} else if (parameter.is(Yal2jvmTreeConstants.JJTSCALARVARIABLE)) {
					fnDesc.addArumentType(VariableType.SCALAR);
					functionScope.addVariable(parameter.getTokenValue(), new VariableDesc(VariableType.SCALAR, true));
				}
			}

		ASTStatements statementsNode = (ASTStatements) node.jjtGetChild(2);

		toAnalyze.add(() -> analyzeStatements(statementsNode, functionScope));
		// TODO -> check if return variable was initialized
	}

	private void analyzeDeclaration(SimpleNode node) {
		if (node.is(Yal2jvmTreeConstants.JJTARRAYVARIABLE)) {
			rootScope.addVariable(node.getTokenValue(), new VariableDesc(VariableType.ARRAY, false));
		} else if (node.is(Yal2jvmTreeConstants.JJTSCALARVARIABLE)) {
			rootScope.addVariable(node.getTokenValue(), new VariableDesc(VariableType.SCALAR, false));
		} else if (node.is(Yal2jvmTreeConstants.JJTDECLARE)) {
			String name = ((SimpleNode) node.jjtGetChild(0)).getTokenValue();
			SimpleNode node2 = (SimpleNode) node.jjtGetChild(1);
			if (node2.is(Yal2jvmTreeConstants.JJTINTEGER))
				rootScope.addVariable(name, new VariableDesc(VariableType.SCALAR, true));
			else if (node2.is(Yal2jvmTreeConstants.JJTARRAY)) {
				VariableDesc var = new VariableDesc(VariableType.ARRAY, true);
				rootScope.addVariable(name, var);
				int size = Integer.parseInt(((SimpleNode) node2.jjtGetChild(0)).getTokenValue());
				var.setSize(size);
			}
		}
	}

	private void analyzeStatements(SimpleNode node, Scope scope) {
		System.out.println(scope);
		for(Node n : node.getChildren()){
			SimpleNode statement = (SimpleNode)n;
			if(statement.is(Yal2jvmTreeConstants.JJTCALL)){
				checkCall(statement);
			} else if(statement.is(Yal2jvmTreeConstants.JJTASSIGN)){
				checkAssign(statement, scope);
			} else if (statement.is(Yal2jvmTreeConstants.JJTWHILE)){
				//TODO: check condition
				System.out.println("while");
				ASTStatements statements =  (ASTStatements)statement.jjtGetChild(1);
				analyzeStatements(statements, new SimpleScope(scope));
			} else if (statement.is(Yal2jvmTreeConstants.JJTIF)){
				//TODO: check condition
				System.out.println("if");
				ASTStatements statements =  (ASTStatements)statement.jjtGetChild(1);
				analyzeStatements(statements, new SimpleScope(scope));
			}
		}
		
	}

	private void checkCall(SimpleNode node) {
		System.out.println("check call");
	}
	
	private void checkAssign(SimpleNode node, Scope scope) {
		SimpleNode access = (SimpleNode)node.jjtGetChild(0);
		VariableDesc desc;
		if(access.is(Yal2jvmTreeConstants.JJTVARIABLE)){
			String name = access.getTokenValue();
			//TODO: check the type
		} else if(access.is(Yal2jvmTreeConstants.JJTARRAYACCESS)){
			//TODO: check the type
		}
		
		SimpleNode assignment = (SimpleNode)node.jjtGetChild(1);
		
		
		
		System.out.println("check assign");
	}
	
	private void checkRhs(SimpleNode node, Scope scope, VariableDesc desc){
		if(node.is(Yal2jvmTreeConstants.JJTARRAY)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTOPERATOR)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTINTEGER)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTNEGATION)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTVARIABLE)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTCALL)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTSIZEOF)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTARRAYACCESS)){
			//TODO: check the type
		} else if(node.is(Yal2jvmTreeConstants.JJTMODULE)){
			//TODO: check the type
		}
	}
	
	

	//
	// private void navigateScope(Runnable runnable, SimpleNode node){
	// boolean changed = false;
	// if (node.is(Yal2jvmTreeConstants.JJTFUNCTION)){
	// System.out.println("+scope");
	// currentScope = new FunctionScope((ModuleScope) currentScope);
	// changed = true;
	// } else if(node.is(Yal2jvmTreeConstants.JJTWHILE) ||
	// node.is(Yal2jvmTreeConstants.JJTIF)){
	// System.out.println("+scope");
	// currentScope = new SimpleScope(currentScope);
	// changed = true;
	// }
	//
	// runnable.run();
	// if(changed){
	// currentScope = currentScope.getParent();
	// System.out.println("-scope");
	// }
	//
	// }

	public static void dump(String prefix, SimpleNode node) {
		System.out.println(prefix + node.toString());
		if (node.getChildren() != null) {
			for (int i = 0; i < node.getChildren().length; ++i) {
				SimpleNode n = (SimpleNode) node.getChildren()[i];
				if (n != null) {
					dump(prefix + " ", n);
				}
			}
		}
	}
}
