package semantic;

import static semantic.Common.cast;
import static semantic.Common.checkSizeOf;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAY;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTASSIGN;
import static yal2jvm.Yal2jvmTreeConstants.JJTCALL;
import static yal2jvm.Yal2jvmTreeConstants.JJTIF;
import static yal2jvm.Yal2jvmTreeConstants.JJTINTEGER;
import static yal2jvm.Yal2jvmTreeConstants.JJTMODULEACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTNEGATION;
import static yal2jvm.Yal2jvmTreeConstants.JJTOPERATOR;
import static yal2jvm.Yal2jvmTreeConstants.JJTSIZEOF;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTWHILE;

import custom.Logger;
import scope.BlockedSimpleScope;
import scope.FunctionDesc;
import scope.Scope;
import scope.ScopeFactory;
import scope.VariableDesc;
import scope.VariableDescFactory;
import scope.VariableType;
import yal2jvm.Node;
import yal2jvm.SimpleNode;

public class StatementsAnalyzer {
	private final Logger LOGGER = Logger.INSTANCE;
	
	
	public void analyzeStatements(SimpleNode node, Scope scope) {
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
				
				checkRhs(lhs, scope, VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false));	
				checkRhs(rhs, scope, VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false));
				
				SimpleNode statements = cast(statement.jjtGetChild(1));
				analyzeStatements(statements, ScopeFactory.INSTANCE.createBlockedScope(scope));
			} else if (statement.is(JJTIF)) {
				LOGGER.semanticInfo(node, "if");
				
				SimpleNode condition = cast(statement.jjtGetChild(0));
				SimpleNode lhs = cast(condition.jjtGetChild(0));
				SimpleNode rhs = cast(condition.jjtGetChild(1));
				
				checkRhs(lhs, scope, VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false));
				checkRhs(rhs, scope, VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false));
				
				BlockedSimpleScope ifScope = ScopeFactory.INSTANCE.createBlockedScope(scope);
				BlockedSimpleScope elseScope = ScopeFactory.INSTANCE.createBlockedScope(scope);
				
				SimpleNode ifStatements = cast(statement.jjtGetChild(1));
				analyzeStatements(ifStatements, ifScope);
				if(statement.jjtGetNumChildren() == 3){
					SimpleNode elseStatements = cast(statement.jjtGetChild(2));
					analyzeStatements(elseStatements, elseScope);
				}
				
				scope.mergeInitialized(ifScope, elseScope);
			}
		}

	}

	private void checkCall(SimpleNode node, Scope scope) {
		SimpleNode nameNode = cast(node.jjtGetChild(0));
		if (nameNode.is(JJTMODULEACCESS)) {
			LOGGER.semanticInfo(node, "call " + nameNode.getTokenValue() + "." + cast(nameNode.jjtGetChild(0)).getTokenValue());
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
			if (!paramDesc.equals(VariableType.SCALAR))
				LOGGER.semanticError(var, "incorrect type");
		} else if (!Common.checkUndeclaredAndUninitialized(scope,var)) {
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
				scope.addVariable(name, VariableDescFactory.INSTANCE.createField(VariableType.ANY, false));
			desc = scope.getVariable(name);
			LOGGER.semanticInfo(access, "set " + name);
			
		} else if (access.is(JJTARRAYACCESS)) {
			// TODO: check the type
			checkArrayAccess(access, scope);
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, true);
		}

		checkRhs(cast(node.jjtGetChild(1)), scope, desc);
	}

	private void checkArrayAccess(SimpleNode node, Scope scope) {
		String name = node.getTokenValue();
		if (!Common.checkUndeclaredAndUninitialized(scope, node)){}
		else {
			String indexValue = cast(node.jjtGetChild(0)).getTokenValue();
			if (!Common.isInt(indexValue) ) {
				if (!Common.checkUndeclaredAndUninitialized(scope, cast(node.jjtGetChild(0)))){}
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
			SimpleNode nameNode = cast(assignment.jjtGetChild(0));
			
			if (nameNode.is(JJTINTEGER)) {
				desc.initialize();
				int size = Integer.parseInt(cast(assignment.jjtGetChild(0)).getTokenValue());
				desc.setType(VariableType.ARRAY);
				
				
				LOGGER.semanticInfo(assignment, "load " + size);
			}
			else if(nameNode.is(JJTSIZEOF)){
				if(Common.checkSizeOf(nameNode, scope))
					desc.initialize();	
			} else {
				String varName = nameNode.getTokenValue();
				if (!scope.hasVariable(varName)) {
					LOGGER.semanticError(assignment, "undeclared"+varName);
					return;
				} else if (!scope.getVariable(varName).is(VariableType.SCALAR)) {
					LOGGER.semanticError(assignment, "incorrect type");
					return;
				} else {
					VariableDesc varDesc = scope.getVariable(varName);
					desc.setType(VariableType.ARRAY);
					desc.initialize();
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
			if (Common.isUninitialized(desc, assignment)) {
				return;
			} else {
				LOGGER.semanticInfo(assignment, "sets all elemets of array");
				desc.setType(VariableType.SCALAR);
			}
		}

		if (assignment.is(JJTVARIABLE)) {
			String name = assignment.getTokenValue();
			if (!Common.checkUndeclaredAndUninitialized(scope, assignment)) {
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
	}

}
