package ir;

import static semantic.Common.cast;
import static semantic.Common.checkSizeOf;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAY;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTASSIGN;
import static yal2jvm.Yal2jvmTreeConstants.JJTCALL;
import static yal2jvm.Yal2jvmTreeConstants.JJTDECLARE;
import static yal2jvm.Yal2jvmTreeConstants.JJTFUNCTION;
import static yal2jvm.Yal2jvmTreeConstants.JJTIF;
import static yal2jvm.Yal2jvmTreeConstants.JJTINTEGER;
import static yal2jvm.Yal2jvmTreeConstants.JJTMODULEACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTNEGATION;
import static yal2jvm.Yal2jvmTreeConstants.JJTOPERATOR;
import static yal2jvm.Yal2jvmTreeConstants.JJTSCALARVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTSIZEOF;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTWHILE;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import custom.Logger;
import scope.BlockedSimpleScope;
import scope.FunctionDesc;
import scope.ModuleScope;
import scope.Scope;
import scope.ScopeFactory;
import scope.VariableDesc;
import scope.VariableDescFactory;
import scope.VariableType;
import semantic.Common;
import yal2jvm.Node;
import yal2jvm.SimpleNode;

public class GenerateCodeModule {
	SimpleNode module;
	ModuleScope rootScope;
	ArrayList<String> register = new ArrayList();
	int limitLocals=10;
	
	
	
	public void ModuleGeneration(SimpleNode module) {
		this.module = module;
		rootScope = ScopeFactory.INSTANCE.getModuleScope();
	}

	Queue<Runnable> toGenerate = new LinkedList<>();

	public void generate() {
		generateModule("", module);
		toGenerate.forEach(r -> r.run());
	}

	public void generateModule(String init, SimpleNode node) {
		init += ".class public " + node.getTokenValue() + System.lineSeparator();
		init += ".super java/lang/Object" + System.lineSeparator();
	  
		for (Node n : node.getChildren())
			if (SimpleNode.class.cast(n).is(JJTFUNCTION))
				generateFunction(init, (SimpleNode) n);
			else
				generateDeclaration(init,SimpleNode.class.cast(n));
		//return init;
	}

	private void generateDeclaration(String init,SimpleNode node) {
		if (node.is(JJTDECLARE)) { // JJTDECLARE
			generateInitialization(init, node);
			return;
		}
		
		VariableDesc desc;
		String name;
		if (node.is(JJTARRAYVARIABLE)) {
			name = node.getTokenValue();
			desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, false);
			//init += ".field static " + name + "[I" + System.lineSeparator();
			
		} else { // JJTSCALARVARIABLE
			name = node.getTokenValue();
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false);
			init += ".field static " + name + "I" + System.lineSeparator();
			
		}
		rootScope.addVariable(name, desc);
	}

	private void generateInitialization(String init, SimpleNode node) {
		VariableDesc desc;
		String name;

		name = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();
		node = (SimpleNode) node.jjtGetChild(1);
		if (node.is(JJTINTEGER)) {
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, true);
			String value = node.jjtGetValue() + node.getTokenValue();
			desc.setValue(Integer.parseInt(value));
			init += ".field static " + name + "I = " + value + System.lineSeparator();
			
			rootScope.addVariable(name, desc);
		} //else { // JJTARRAY
		
	}

	

	private void generateFunction(String init, SimpleNode node) {
		
		
		for(int i=0; i<limitLocals; i++){ register.add(null);}

		Scope functionScope = ScopeFactory.INSTANCE.createSimpleScope(rootScope);
		FunctionDesc fnDesc = new FunctionDesc("");
		init += ".method public static " + node.getTokenValue();
		//Optional<VariableDesc> returnVar = analyzeFunctionReturn((SimpleNode) node.jjtGetChild(0), fnDesc,
		//		functionScope);
		generateFunctionParameters(init, (SimpleNode) node.jjtGetChild(1), fnDesc, functionScope);
		init += ".limit stack 10"  + System.lineSeparator();
		init += ".limit locals 10 "  + System.lineSeparator();
		rootScope.addFunction(node.getTokenValue(), fnDesc);
		SimpleNode statementsNode = (SimpleNode) node.jjtGetChild(2);
		final String init2 = init;
		toGenerate.add(() -> {
			//.semanticInfo(node, "function: " + node.getTokenValue());

		generateStatements(init2, statementsNode, functionScope);


			// TODO -> WARNING parameters

		});

	}

	private Optional<VariableDesc> generateFunctionReturn(String init, SimpleNode returnNode, FunctionDesc fnDesc, Scope functionScope) {
		VariableDesc retVar = null;
		if (returnNode.jjtGetNumChildren() == 0) {
			fnDesc.setReturnType(VariableType.NULL);
		} else {
			returnNode = (SimpleNode) returnNode.jjtGetChild(0);
			if (returnNode.is(JJTARRAYVARIABLE)) {
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY, false);
				fnDesc.setReturnType(VariableType.ARRAY);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			} else if (returnNode.is(JJTSCALARVARIABLE)) {
				fnDesc.setReturnType(VariableType.SCALAR);
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			}
		}
		return Optional.ofNullable(retVar);
	}

	private void generateFunctionParameters(String init, SimpleNode parametersNode, FunctionDesc fnDesc, Scope functionScope) {
		
		if (parametersNode.getChildren() == null){
		init += "([Ljava/lang/String;)" /*+ retarnValue*/+ System.lineSeparator();
		}else{
			for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
		        register.set(register.indexOf(null), parameter.getTokenValue());
		}
		String sParameter = "I";
		String repeated = new String(new char[parametersNode.jjtGetNumChildren()]).replace("\0", sParameter);
		init += "(" + repeated + ")" /*+ retarnValue*/+ System.lineSeparator();
		
		}
		
		
			
	/*		for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
				if (parameter.is(JJTSCALARVARIABLE)) {
					fnDesc.addArumentType(VariableType.SCALAR);
					functionScope.addVariable(parameter.getTokenValue(), VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, true));
				 
				}
			}*/
	}
	
	public void generateStatements(String init, SimpleNode node, Scope scope) {
		if (node.jjtGetNumChildren() == 0)
			return;
		for (Node n : node.getChildren()) {
			SimpleNode statement = (SimpleNode) n;

			if (statement.is(JJTCALL)) {
				generateCall(init, statement, scope);
			} else if (statement.is(JJTASSIGN)) {
				generateAssign(init, statement, scope);
			} 
		}

	}

	private void generateCall(String init, SimpleNode node, Scope scope) {
		SimpleNode nameNode = cast(node.jjtGetChild(0));
		if (nameNode.is(JJTMODULEACCESS)) {
			init += "invokestatic" + nameNode.getTokenValue() + "/" + cast(nameNode.jjtGetChild(0)).getTokenValue();
			
		}

		String name = nameNode.getTokenValue();
		FunctionDesc desc = scope.getFunction(name);
		SimpleNode argumentsNode = SimpleNode.class.cast(node.jjtGetChild(1));

		generateFunctionParameters(init, argumentsNode, desc, scope);

	}

	

	private void generateAssign(String init, SimpleNode node, Scope scope) {
		SimpleNode access = (SimpleNode) node.jjtGetChild(0);
		VariableDesc desc = null;
		String name = access.getTokenValue();
		int index=0;
		if (access.is(JJTVARIABLE)) {
			if (!scope.hasVariable(name))
				scope.addVariable(name, VariableDescFactory.INSTANCE.createField(VariableType.ANY, false));
			desc = scope.getVariable(name);
			register.set(register.indexOf(null), access.getTokenValue());
			//maybe theres a better way to do this
			for (int i=0;i<limitLocals;i++){
				if(register.get(i)== access.getTokenValue()){
					index=i;
				}
			}
			init += "istore_" + index +  System.lineSeparator();
			
			
		} 

		generateRhs(init, cast(node.jjtGetChild(1)), scope, desc);
	}

	

	private void generateRhs(String init, SimpleNode assignment, Scope scope, VariableDesc desc) {
		// SimpleNode access = (SimpleNode) node.jjtGetChild(0);

		

		if (assignment.is(JJTVARIABLE)) {
			String name = assignment.getTokenValue();
			if (!Common.checkUndeclaredAndUninitialized(scope, assignment)) {
				return;
			} else if (desc.is(VariableType.SCALAR) && scope.getVariable(name).is(VariableType.ARRAY)) {
				//LOGGER.semanticError(assignment, "incorrect type " + name);
				return;
			} else if (desc.is(VariableType.ANY)) {
				desc.setType(scope.getVariable(name).getType());
			}
			//LOGGER.semanticInfo(assignment, "load " + name);
		} else if (assignment.is(JJTINTEGER)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			//LOGGER.semanticInfo(assignment, "load " + assignment.getTokenValue());
		} else if (assignment.is(JJTOPERATOR)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			//LOGGER.semanticInfo(assignment, assignment.getTokenValue());
			generateRhs(init,cast(assignment.jjtGetChild(0)), scope, desc);
			generateRhs(init, cast(assignment.jjtGetChild(1)), scope, desc);
		} else if (assignment.is(JJTNEGATION)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			//LOGGER.semanticInfo(assignment, "negation");
			generateRhs(init, cast(assignment.jjtGetChild(0)), scope, desc);
		} else if (assignment.is(JJTCALL)) {
			generateCall(init, assignment, scope);
		} else if (assignment.is(JJTSIZEOF)) {
			checkSizeOf(assignment, scope);
		} else if (assignment.is(JJTMODULEACCESS)) {
			//LOGGER.semanticInfo(assignment,
			//		"load " + assignment.getTokenValue() + "." + cast(assignment.jjtGetChild(0)).getTokenValue());
		}

		desc.initialize(); // after correct assignment
	}

}