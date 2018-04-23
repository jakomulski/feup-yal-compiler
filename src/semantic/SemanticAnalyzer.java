package semantic;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import custom.Logger;
import scope.FunctionDesc;
import scope.ModuleScope;
import scope.Scope;
import scope.ScopeFactory;
import scope.VariableDesc;
import scope.VariableType;
import yal2jvm.ASTFunction;
import yal2jvm.ASTStatements;
import yal2jvm.Node;
import yal2jvm.SimpleNode;
import static yal2jvm.Yal2jvmTreeConstants.*;

public class SemanticAnalyzer {
	private static final String IS_INT = "\\d+";
	private final Logger LOGGER = Logger.INSTANCE;
	SimpleNode module;
	ModuleScope rootScope;

	public SemanticAnalyzer(SimpleNode module) {
		this.module = module;
		rootScope = ScopeFactory.INSTANCE.getModuleScope();
	}

	Queue<Runnable> toAnalyze = new LinkedList<>();

	public void analyze() {
		analyzeModule("", module);
		toAnalyze.forEach(r -> r.run());
	}

	public void analyzeModule(String prefix, SimpleNode node) {
		LOGGER.semanticInfo(node, "class " + node.getTokenValue());
		for (Node n : node.getChildren())
			if (SimpleNode.class.cast(n).is(JJTFUNCTION))
				analyzeFunction((ASTFunction) n);
			else
				analyzeDeclaration(SimpleNode.class.cast(n));
	}

	private void analyzeDeclaration(SimpleNode node) {
		if (node.is(JJTDECLARE)) { // JJTDECLARE
			analyzeInitialization(node);
			return;
		}

		VariableDesc desc;
		String name;
		if (node.is(JJTARRAYVARIABLE)) {
			name = node.getTokenValue();
			desc = new VariableDesc(VariableType.ARRAY, false);
			LOGGER.semanticInfo(node, "" + name + "[]");

		} else { // JJTSCALARVARIABLE
			name = node.getTokenValue();
			desc = new VariableDesc(VariableType.SCALAR, false);
			LOGGER.semanticInfo(node, "" + name);
		}
		rootScope.addVariable(name, desc);
	}

	private void analyzeInitialization(SimpleNode node) {
		VariableDesc desc;
		String name;

		name = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();
		node = (SimpleNode) node.jjtGetChild(1);
		if (node.is(JJTINTEGER)) {
			desc = new VariableDesc(VariableType.SCALAR, true);
			String value = node.jjtGetValue() + node.getTokenValue();
			desc.setValue(Integer.parseInt(value));
			LOGGER.semanticInfo(node, "" + name + "=" + value);
			rootScope.addVariable(name, desc);
		} else { // JJTARRAY
			analyzeArrayInitialization((SimpleNode) node.jjtGetChild(0), name);
		}
	}



	private void analyzeArrayInitialization(SimpleNode node, String name) {
		VariableDesc desc;
		if (node.is(JJTINTEGER)) {
			int size = Integer.parseInt(node.getTokenValue());

			desc = new VariableDesc(VariableType.ARRAY, true).withSize(size);
			LOGGER.semanticInfo(node, "" + name + "=" + "[" + size + "]");
		} else if (node.is(JJTSIZEOF)) {
			if(!checkSizeOf(node, rootScope))
				return;
			else
			{
				VariableDesc varDesc = rootScope.getVariable(SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue());
				desc = new VariableDesc(VariableType.ARRAY, true).withSize(varDesc.getSize());
				LOGGER.semanticInfo(node, name + "=" + "[" + varDesc.getSize() + "]");
			}
		} else {
			String varName = node.getTokenValue();
			if (!rootScope.hasVariable(varName)) {
				LOGGER.semanticError(node, "undeclared");
				return;
			} else if (!rootScope.getVariable(varName).is(VariableType.SCALAR)) {
				LOGGER.semanticError(node, "incorrect type");
				return;
			} else {
				VariableDesc varDesc = rootScope.getVariable(varName);
				int size = varDesc.getValue();
				if (size < 1) {
					LOGGER.semanticError(node, "size of array has to be > 0");
					return;
				}
				desc = new VariableDesc(VariableType.ARRAY, true).withSize(size);
				LOGGER.semanticInfo(node, "" + name + "=" + "[" + size + "]");
			}
		}
		rootScope.addVariable(name, desc);
	}
	
	private boolean checkSizeOf(SimpleNode node, Scope scope) {
		String varName = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();

		if (!scope.hasVariable(varName)) {
			LOGGER.semanticError(node, "undeclared");
			return false;
		} else if (!scope.getVariable(varName).is(VariableType.ARRAY)) {
			LOGGER.semanticError(node, "incorrect type");
			return false;
		}
		return true;
	}
	
	private void analyzeFunction(ASTFunction node) {

		Scope functionScope = ScopeFactory.INSTANCE.createSimpleScope(rootScope);
		FunctionDesc fnDesc = new FunctionDesc();

		Optional<VariableDesc> returnVar = analyzeFunctionReturn((SimpleNode) node.jjtGetChild(0), fnDesc,
				functionScope);
		analyzeFunctionParameters((SimpleNode) node.jjtGetChild(1), fnDesc, functionScope);

		rootScope.addFunction(node.getTokenValue(), fnDesc);
		ASTStatements statementsNode = (ASTStatements) node.jjtGetChild(2);
		toAnalyze.add(() -> {
			LOGGER.semanticInfo(node, "function: " + node.getTokenValue());

			analyzeStatements(statementsNode, functionScope);

			returnVar.ifPresent(desc -> {
				if (!desc.isInitialized()) // ERROR
					LOGGER.semanticError(statementsNode, "return value is not initialized");
			});

			// TODO -> WARNING parameters

		});

	}

	private Optional<VariableDesc> analyzeFunctionReturn(SimpleNode returnNode, FunctionDesc fnDesc,
			Scope functionScope) {
		VariableDesc retVar = null;
		if (returnNode.jjtGetNumChildren() == 0) {
			fnDesc.setReturnType(VariableType.NULL);
		} else {
			returnNode = (SimpleNode) returnNode.jjtGetChild(0);
			if (returnNode.is(JJTARRAYVARIABLE)) {
				retVar = new VariableDesc(VariableType.ARRAY, false);
				fnDesc.setReturnType(VariableType.ARRAY);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			} else if (returnNode.is(JJTSCALARVARIABLE)) {
				fnDesc.setReturnType(VariableType.SCALAR);
				retVar = new VariableDesc(VariableType.SCALAR, false);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			}
		}
		return Optional.ofNullable(retVar);
	}

	private void analyzeFunctionParameters(SimpleNode parametersNode, FunctionDesc fnDesc, Scope functionScope) {
		if (parametersNode.getChildren() != null)
			for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
				if (parameter.is(JJTARRAYVARIABLE)) {
					fnDesc.addArumentType(VariableType.ARRAY);
					functionScope.addVariable(parameter.getTokenValue(), new VariableDesc(VariableType.ARRAY, true));
				} else if (parameter.is(JJTSCALARVARIABLE)) {
					fnDesc.addArumentType(VariableType.SCALAR);
					functionScope.addVariable(parameter.getTokenValue(), new VariableDesc(VariableType.SCALAR, true));
				}
			}
	}

	private void analyzeStatements(SimpleNode node, Scope scope) {
		if (node.jjtGetNumChildren() == 0)
			return;
		for (Node n : node.getChildren()) {
			SimpleNode statement = (SimpleNode) n;

			if (statement.is(JJTCALL)) {
				checkCall(statement, scope);
			} else if (statement.is(JJTASSIGN)) {
				checkAssign(statement, scope);
			} else if (statement.is(JJTWHILE)) {
				LOGGER.semanticInfo(node, "loop");
				
				SimpleNode condition = cast(statement.jjtGetChild(0));
				SimpleNode lhs = cast(condition.jjtGetChild(0));
				SimpleNode rhs = cast(condition.jjtGetChild(1));
				
				checkRhs(lhs, scope, new VariableDesc(VariableType.SCALAR, false));	
				checkRhs(rhs, scope, new VariableDesc(VariableType.SCALAR, false));
				
				SimpleNode statements = cast(statement.jjtGetChild(1));
				analyzeStatements(statements, ScopeFactory.INSTANCE.createSimpleScope(scope));
			} else if (statement.is(JJTIF)) {
				LOGGER.semanticInfo(node, "if");
				
				SimpleNode condition = cast(statement.jjtGetChild(0));
				SimpleNode lhs = cast(condition.jjtGetChild(0));
				SimpleNode rhs = cast(condition.jjtGetChild(1));
				
				checkRhs(lhs, scope, new VariableDesc(VariableType.SCALAR, false));
				checkRhs(rhs, scope, new VariableDesc(VariableType.SCALAR, false));
				
				SimpleNode statements = cast(statement.jjtGetChild(1));
				analyzeStatements(statements, ScopeFactory.INSTANCE.createSimpleScope(scope));
			}
		}

	}

	private void checkCall(SimpleNode node, Scope scope) {

		SimpleNode nameNode = SimpleNode.class.cast(node.jjtGetChild(0));

		if (nameNode.is(JJTMODULEACCESS)) {
			LOGGER.semanticInfo(node,
					"call " + nameNode.getTokenValue() + "." + cast(nameNode.jjtGetChild(0)).getTokenValue());
			return; // CANNOT BE CHECKED
		}

		String name = nameNode.getTokenValue();

		if (!scope.hasFunction(name)) {
			LOGGER.semanticError(node, "missing funtion");
			return;
		}

		FunctionDesc desc = scope.getFunction(name);
		SimpleNode argumentsNode = SimpleNode.class.cast(node.jjtGetChild(1));

		if (desc.getParamsNum() != argumentsNode.jjtGetNumChildren()) {
			LOGGER.semanticError(node, "incorrect number of parameters");
		} else {
			for (int i = 0; i < desc.getParamsNum(); ++i) {
				checkArgument((SimpleNode) argumentsNode.jjtGetChild(i), scope, desc.getArumentsTypes().get(i));
			}
		}

	}

	private void checkArgument(SimpleNode var, Scope scope, VariableType paramDesc) {
		String name = var.getTokenValue();

		if (var.is(JJTINTEGER)) {
			if (paramDesc != VariableType.SCALAR)
				LOGGER.semanticError(var, "incorrect type");
		} else if (!scope.hasVariable(name)) {
			LOGGER.semanticError(var, "undeclared");
		} else if (!scope.getVariable(name).isInitialized()) {
			LOGGER.semanticError(var, "not initialized");
		} else if (!scope.getVariable(name).getType().equals(paramDesc)) {
			LOGGER.semanticError(var, "incorrect type");
		}
	}

	private void checkAssign(SimpleNode node, Scope scope) {
		SimpleNode access = (SimpleNode) node.jjtGetChild(0);
		VariableDesc desc = null;
		String name = access.getTokenValue();

		if (access.is(JJTVARIABLE)) {
			if (!scope.hasVariable(name))
				scope.addVariable(name, new VariableDesc(VariableType.ANY, false));
			desc = scope.getVariable(name);
			LOGGER.semanticInfo(access, "set " + name);
		} else if (access.is(JJTARRAYACCESS)) {
			// TODO: check the type
			checkArrayAccess(access, scope);
			desc = new VariableDesc(VariableType.SCALAR, true);
		}

		checkRhs(cast(node.jjtGetChild(1)), scope, desc);
		// System.out.println(scope);
	}

	private void checkArrayAccess(SimpleNode node, Scope scope) {
		String name = node.getTokenValue();
		if (!scope.hasVariable(name))
			LOGGER.semanticError(node, "undeclared");
		else if (!scope.getVariable(name).isInitialized())
			LOGGER.semanticError(node, "not initialized");
		else {
			String indexValue = cast(node.jjtGetChild(0)).getTokenValue();
			if (!indexValue.matches(IS_INT)) {
				if (!scope.hasVariable(indexValue))
					LOGGER.semanticError(node, "undeclared");
				else if (!scope.getVariable(indexValue).isInitialized())
					LOGGER.semanticError(node, "not initialized");
				else if (scope.getVariable(indexValue).is(scope.getVariable(name).getType()))
					LOGGER.semanticError(node, "wrong type");
				else
					LOGGER.semanticInfo(node, "set " + name + "[" + indexValue + "]");
			} else
				LOGGER.semanticInfo(node, "set " + name + "[" + indexValue + "]");
		}
	}

	private void checkRhsArrayAssign(SimpleNode assignment, Scope scope, VariableDesc desc) {
		if (desc.is(VariableType.ANY) || desc.is(VariableType.ARRAY)) {
			desc.setType(VariableType.ARRAY);
			desc.setInitialized(true);
			if (SimpleNode.class.cast(assignment.jjtGetChild(0)).is(JJTINTEGER)) {
				int size = Integer.parseInt(SimpleNode.class.cast(assignment.jjtGetChild(0)).getTokenValue());
				desc = new VariableDesc(VariableType.ARRAY, true).withSize(size);
				LOGGER.semanticInfo(assignment, "load " + size);
			} else {
				String varName = SimpleNode.class.cast(assignment.jjtGetChild(0)).getTokenValue();
				System.out.println(varName);
				if (!scope.hasVariable(varName)) {
					LOGGER.semanticError(assignment, "undeclared");
					return;
				} else if (!scope.getVariable(varName).is(VariableType.SCALAR)) {
					LOGGER.semanticError(assignment, "incorrect type");
					return;
				} else {
					VariableDesc varDesc = scope.getVariable(varName);
					desc = new VariableDesc(VariableType.ARRAY, true).withSize(VariableDesc.ANY_SIZE);
					LOGGER.semanticInfo(assignment, "load " + varName);
				}
			}
			LOGGER.semanticInfo(assignment, "set newarray");
		} else {
			LOGGER.semanticError(assignment, "incorrect type");
		}
	}

	private void checkRhs(SimpleNode assignment, Scope scope, VariableDesc desc) {
		// SimpleNode access = (SimpleNode) node.jjtGetChild(0);

		if (assignment.is(JJTARRAY)) {
			checkRhsArrayAssign(assignment, scope, desc);
			return;
		} else if (desc.is(VariableType.ARRAY)) { // sets all elements of array
			if (!desc.isInitialized()) {
				LOGGER.semanticError(assignment, "not initialized");
				return;
			} else {
				LOGGER.semanticInfo(assignment, "sets all elemets of array");
				desc.setType(VariableType.SCALAR);
			}
		}

		if (assignment.is(JJTVARIABLE)) {
			String name = assignment.getTokenValue();
			if (!scope.hasVariable(name)) {
				LOGGER.semanticError(assignment, "undeclared");
				return;
			} else if (!scope.getVariable(name).isInitialized()) {
				LOGGER.semanticError(assignment, "not initialized");
				return;
			} else if (desc.is(VariableType.SCALAR) && scope.getVariable(name).is(VariableType.ARRAY)) {
				LOGGER.semanticError(assignment, "incorrect type " + name);
				return;
			} else if (desc.is(VariableType.ANY)) {
				desc.setType(scope.getVariable(name).getType());
			}
			LOGGER.semanticInfo(assignment, "load " + name);
		} else if (assignment.is(JJTINTEGER)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			LOGGER.semanticInfo(assignment, "load " + assignment.getTokenValue());
		} else if (assignment.is(JJTOPERATOR)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			LOGGER.semanticInfo(assignment, assignment.getTokenValue());
			checkRhs(cast(assignment.jjtGetChild(0)), scope, desc);
			checkRhs(cast(assignment.jjtGetChild(1)), scope, desc);
		} else if (assignment.is(JJTNEGATION)) {
			if (desc.is(VariableType.ANY))
				desc.setType(VariableType.SCALAR);
			LOGGER.semanticInfo(assignment, "negation");
			checkRhs(cast(assignment.jjtGetChild(0)), scope, desc);
		} else if (assignment.is(JJTCALL)) {
			checkCall(assignment, scope);
		} else if (assignment.is(JJTSIZEOF)) {
			checkSizeOf(assignment, scope);
		} else if (assignment.is(JJTARRAYACCESS)) {
			checkArrayAccess(assignment, scope);
		} else if (assignment.is(JJTMODULEACCESS)) {
			LOGGER.semanticInfo(assignment,
					"load " + assignment.getTokenValue() + "." + cast(assignment.jjtGetChild(0)).getTokenValue());
		}

		desc.initialize(); // after correct assignment

		// for(Node child : node.getChildren()){
		// checkRhs(child, scope, desc);
		// }
	}

	public static void dump(String prefix, SimpleNode node) {
		System.out.println(prefix + node.toString() + " " + node.getTokenValue());
		if (node.getChildren() != null) {
			for (int i = 0; i < node.getChildren().length; ++i) {
				SimpleNode n = (SimpleNode) node.getChildren()[i];
				if (n != null) {
					dump(prefix + " ", n);
				}
			}
		}
	}

	private SimpleNode cast(Node node) {
		return SimpleNode.class.cast(node);
	}
}
