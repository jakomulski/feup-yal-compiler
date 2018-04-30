package ir;

import static yal2jvm.Yal2jvmTreeConstants.*;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTASSIGN;
import static yal2jvm.Yal2jvmTreeConstants.JJTCALL;
import static yal2jvm.Yal2jvmTreeConstants.JJTINTEGER;
import static yal2jvm.Yal2jvmTreeConstants.JJTMODULEACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTNEGATION;
import static yal2jvm.Yal2jvmTreeConstants.JJTOPERATOR;
import static yal2jvm.Yal2jvmTreeConstants.JJTSIZEOF;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import custom.StackSizeCounter;
import scope.FunctionDesc;
import scope.Scope;
import scope.VariableDesc;
import scope.VariableType;
import semantic.Common;
import yal2jvm.Node;
import yal2jvm.SimpleNode;

public class IrBuilder {
	private final StackSizeCounter stackSizeCounter;

	private final FunctionDesc functionDesc;
	private final List<Statement> statements = new ArrayList<>();

	private final List<VariableDesc> localVariables = new ArrayList<>();
	private final List<VariableDesc> parameters = new ArrayList<>();

	public IrBuilder(FunctionDesc desc) {
		this.functionDesc = desc;
		stackSizeCounter = new StackSizeCounter();
	}

	public void addVariable(VariableDesc desc) {
		localVariables.add(desc);
	}

	public void addParameter(VariableDesc desc) {
		parameters.add(desc);
	}

	public void addReturnValue(VariableDesc desc) {
		parameters.add(desc);
		Statement statement = new Statement(new SimpleNode(-1), null);
		statements.add(statement);
		if (desc.getType().equals(VariableType.NULL))
			statement.returnn();
		else {
			statement.iload(desc);
			statement.ireturn();
		}
	}

	public Statement addStatement(SimpleNode node, Scope scope) {
		Statement statement = new Statement(node, scope);
		statement.setRef(statement);
		statement.setStackSizeCounter(stackSizeCounter);
		statements.add(statement);
		return statement;
	}

	public void addGoToStatement(Statement statement) {
		statement.append(() -> "goto " + statement.getRef().getName());
		statements.add(statement);
	}

	public Statement addLabelStatement(Statement statement) {
		statement.append(() -> statement.getName() + ":");
		statements.add(statement);
		return statement;
	}

	public List<String> build() {
		registerAlocation();

		List<String> lines = new ArrayList<>();
		generate(lines);
		return lines;
	}

	private void registerAlocation() {
		AtomicInteger counter = new AtomicInteger(0); // dummy alocation
		parameters.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
		localVariables.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
	}

	private void generate(List<String> lines) {
		List<Supplier<String>> supLines = new ArrayList<>();
		
		supLines.add(()->".method public static " + functionDesc.asJasmin());
		int numberOfLocals = parameters.size() + localVariables.size();
		supLines.add(()->".limit locals " + Integer.toString(numberOfLocals));
		supLines.add(()->".limit stack "+stackSizeCounter.getStackSize());
		statements.forEach(statement -> {
			generateStatement(statement, statement.getNode());
			supLines.addAll(statement.getValue());
			supLines.add(()->"");
		});
		supLines.add(()->".end method");
		
		lines.addAll(supLines.stream().map(l->l.get()).collect(Collectors.toList()));
	}

	private void generateStatement(Statement statement, SimpleNode node) {
		if (node.is(-1))
			return;

		if (node.is(JJTCALL)) {
			generateCall(statement, node);
			// TODO: pop from stack if not void
		} else if (node.is(JJTASSIGN))
			generateAssign(statement, node);
		else if (node.is(JJTWHILE))
			generateLoop(statement, node);
		else if (node.is(JJTIF))
			generateIf(statement, node);

	}

	private void generateLoop(Statement statement, SimpleNode node) {
		SimpleNode conditionNode = node.jjtGetChild(0);
		String condition = conditionNode.getTokenValue();
		generateRHS(statement, conditionNode.jjtGetChild(0));
		generateRHS(statement, conditionNode.jjtGetChild(1));
		statement.ificmp(condition);
	}

	private void generateIf(Statement statement, SimpleNode node) {
		SimpleNode conditionNode = node.jjtGetChild(0);
		String condition = conditionNode.getTokenValue();
		generateRHS(statement, conditionNode.jjtGetChild(0));
		generateRHS(statement, conditionNode.jjtGetChild(1));
		statement.ificmp(condition);
	}

	private void generateAssign(Statement statement, SimpleNode node) {
		SimpleNode var = node.jjtGetChild(0);

		if (var.is(JJTARRAYACCESS)) {
			String name = var.getTokenValue();
			String index = var.jjtGetChild(0).getTokenValue();
			statement.aload(statement.scope.getVariable(name));
			if (Common.isInt(index))
				statement.bipush(index);
			else
				statement.iload(statement.scope.getVariable(index));

			generateRHS(statement, (SimpleNode) node.jjtGetChild(1));
			statement.iastore();
			return;
		}

		generateRHS(statement, (SimpleNode) node.jjtGetChild(1));

		String varName = var.getTokenValue();
		VariableDesc varDesc = statement.scope.getVariable(varName);

		if (varDesc.isField())
			statement.putstatic(varDesc);
		else if (varDesc.is(VariableType.SCALAR))
			statement.istore(varDesc);
		else if (varDesc.is(VariableType.ANY)) // functions by default return
												// scalar
			statement.istore(varDesc);
		else if (varDesc.is(VariableType.ARRAY))
			statement.astore(varDesc);

		statement.setType(varDesc);

	}

	private void generateRHS(Statement statement, SimpleNode assignment) {
		if (assignment.is(JJTARRAY)) {
			generateRHS(statement, assignment.jjtGetChild(0));
			statement.newarray();
		} else if (assignment.is(JJTVARIABLE)) {
			String name = assignment.getTokenValue();
			if (statement.scope.getVariable(name).is(VariableType.SCALAR))
				statement.iload(statement.scope.getVariable(assignment.getTokenValue()));
			else {
				statement.aload(statement.scope.getVariable(assignment.getTokenValue()));
			}
		} else if (assignment.is(JJTINTEGER)) {
			statement.bipush(assignment.getTokenValue());
		} else if (assignment.is(JJTOPERATOR)) {
			generateRHS(statement, assignment.jjtGetChild(0));
			generateRHS(statement, assignment.jjtGetChild(1));
			statement.operator(assignment.getTokenValue());
		} else if (assignment.is(JJTNEGATION)) {
			generateRHS(statement, assignment.jjtGetChild(0));
			statement.ineg();
		} else if (assignment.is(JJTCALL)) {
			generateCall(statement, assignment);
		} else if (assignment.is(JJTSIZEOF)) {
			statement.aload(statement.scope.getVariable(assignment.jjtGetChild(0).getTokenValue())); // Sizeof
			// ->
			// Variable
			statement.arraylength();
		} else if (assignment.is(JJTARRAYACCESS)) {
			String name = assignment.getTokenValue();
			String index = assignment.jjtGetChild(0).getTokenValue();
			statement.aload(statement.scope.getVariable(name));
			if (Common.isInt(index))
				statement.bipush(index);
			else
				statement.iload(statement.scope.getVariable(index));
			statement.iaload();
		} else if (assignment.is(JJTMODULEACCESS)) {
			// IMPOSSIBLE
		} else if (assignment.is(JJTSTRING)) {
			statement.ldc(assignment.getTokenValue());
		}
	}

	private void loadArguments(Statement statement, SimpleNode node) {
		if (node.jjtGetNumChildren() > 0)
			for (Node n : node.getChildren()) {
				generateRHS(statement, (SimpleNode) n);
			}
	}

	private void generateModuleCall(Statement statement, SimpleNode node) {
		SimpleNode moduleAccessNode = node.jjtGetChild(0);
		String module = moduleAccessNode.getTokenValue();
		String name = moduleAccessNode.jjtGetChild(0).getTokenValue();

		List<String> args = new ArrayList<>();

		SimpleNode argsNode = node.jjtGetChild(1);
		if (argsNode.jjtGetNumChildren() > 0)
			for (Node n : argsNode.getChildren()) {
				generateRHS(statement, (SimpleNode) n);
				args.add(parseArg(statement, (SimpleNode) n));
			}

		statement.invokestatic(module, name, args);
	}

	private String parseArg(Statement statement, SimpleNode node) {
		if (node.is(JJTSTRING))
			return "Ljava/lang/String;";
		if (node.is(JJTVARIABLE)) {
			String name = node.getTokenValue();
			if (statement.scope.getVariable(name).is(VariableType.SCALAR))
				return "I";
			return "[I";
		}
		if (node.is(JJTINTEGER))
			return "I";
		return "";
	}

	private void generateCall(Statement statement, SimpleNode node) {
		if (node.jjtGetChild(0).is(JJTMODULEACCESS)) {
			generateModuleCall(statement, node);
			return;
		}
		loadArguments(statement, node.jjtGetChild(1));
		String name = node.jjtGetChild(0).getTokenValue();
		statement.invokestatic(name);

	}
}
