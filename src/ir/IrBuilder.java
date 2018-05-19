package ir;

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
import static yal2jvm.Yal2jvmTreeConstants.JJTSTRING;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTWHILE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import custom.StackSizeCounter;
import operations.ALoad;
import operations.AReturn;
import operations.AStore;
import operations.AddOperation;
import operations.ArrayLength;
import operations.BiPush;
import operations.GetStatic;
import operations.GoTo;
import operations.IALoad;
import operations.IAStore;
import operations.ILoad;
import operations.INeg;
import operations.IReturn;
import operations.IStore;
import operations.IfIcmp;
import operations.InvokeStatic;
import operations.Label;
import operations.Ldc;
import operations.NewArray;
import operations.Operator;
import operations.PutStatic;
import operations.Return;
import optimization.Optimizer;
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

    public List<String> build(Optimizer optimizer) {
        statements.forEach(statement -> {
            generateStatement(statement);
            statement.optimize();
        });

        optimizer.optimize(statements);

        registerAlocation();
        List<Supplier<String>> lines = new ArrayList<>();
        generate(lines);
        return lines.stream().map(s -> s.get()).collect(Collectors.toList());
    }

    private void generate(List<Supplier<String>> supLines) {
        supLines.add(() -> ".method public static " + functionDesc.asJasmin());
        int numberOfLocals = parameters.size() + localVariables.size();
        // supLines.add(() -> ".limit locals " + parameters.stream().map(p ->
        // p.getName()).collect(Collectors.toList())
        // + " " + localVariables.stream().map(p ->
        // p.getName()).collect(Collectors.toList()));
        supLines.add(() -> ".limit locals " + Integer.toString(numberOfLocals));
        supLines.add(() -> ".limit stack " + stackSizeCounter.getStackSize());
        statements.forEach(statement -> {
            supLines.add(() -> statement.toString());
            supLines.add(() -> "");
            statement.calculateStackSize(stackSizeCounter);
        });
        supLines.add(() -> ".end method");
    }

    private void registerAlocation() {
        AtomicInteger counter = new AtomicInteger(0); // dummy alocation
        parameters.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
        localVariables.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
    }

    public void addVariable(VariableDesc desc) {
        localVariables.add(desc);
    }

    public void addParameter(VariableDesc desc) {
        parameters.add(desc);
    }

    public void addReturnValue(VariableDesc desc) {
        Statement statement = new Statement();
        statements.add(statement);
        if (desc.getType().equals(VariableType.NULL))
            statement.add(new Return());
        else if (desc.is(VariableType.ARRAY)) {
            statement.add(new AReturn()).add(new ALoad(desc));
            parameters.add(desc);
        } else {
            statement.add(new IReturn()).add(new ILoad(desc));
            parameters.add(desc);
        }
    }

    public Statement addStatement(SimpleNode node, Scope scope) {
        Statement statement = new Statement(node, scope, stackSizeCounter);
        statement.setRef(statement);
        statements.add(statement);
        return statement;
    }

    public void addGoToStatement(Statement statement) {
        statement.add(new GoTo(statement.getRef()));
        statements.add(statement);
    }

    public Statement addLabelStatement(Statement statement) {
        statement.add(new Label(statement));
        statements.add(statement);
        return statement;
    }

    private void generateStatement(Statement statement) {
        SimpleNode node = statement.getNode();

        if (node.is(-1))
            return;

        if (node.is(JJTCALL)) {
            generateCall(statement.scope, node, statement);
            // TODO: pop from stack if not void
        } else if (node.is(JJTASSIGN))
            generateAssign(statement.scope, node, statement);
        else if (node.is(JJTWHILE))
            generateLoop(statement.scope, node, statement);
        else if (node.is(JJTIF))
            generateIf(statement.scope, node, statement);

    }

    private void generateLoop(Scope scope, SimpleNode node, AddOperation addOperation) {
        SimpleNode conditionNode = node.jjtGetChild(0);
        String condition = conditionNode.getTokenValue();
        addOperation = addOperation.add(new IfIcmp(condition));
        generateRHS(scope, conditionNode.jjtGetChild(0), addOperation);
        generateRHS(scope, conditionNode.jjtGetChild(1), addOperation);
    }

    private void generateIf(Scope scope, SimpleNode node, AddOperation addOperation) {
        SimpleNode conditionNode = node.jjtGetChild(0);
        String condition = conditionNode.getTokenValue();
        addOperation = addOperation.add(new IfIcmp(condition));
        generateRHS(scope, conditionNode.jjtGetChild(0), addOperation);
        generateRHS(scope, conditionNode.jjtGetChild(1), addOperation);
    }

    private void generateAssign(Scope scope, SimpleNode node, Statement statement) {
        SimpleNode var = node.jjtGetChild(0);
        String varName = var.getTokenValue();
        if (var.is(JJTARRAYACCESS)) {
            String index = var.jjtGetChild(0).getTokenValue();
            AddOperation addOperation = statement.add(new IAStore());
            addOperation.add(new ALoad(scope.getVariable(varName)));
            if (Common.isInt(index))
                addOperation.add(new BiPush(index));
            else
                addOperation.add(new ILoad(scope.getVariable(index)));
            generateRHS(scope, node.jjtGetChild(1), addOperation);
        } else {
            VariableDesc varDesc = scope.getVariable(varName);
            if (varDesc.isField()) {
                generateRHS(scope, node.jjtGetChild(1), statement.add(new PutStatic(scope.getModuleName(), varDesc)));
            } else if (varDesc.is(VariableType.SCALAR) || varDesc.is(VariableType.ANY)) {
                generateRHS(scope, node.jjtGetChild(1), statement.add(new IStore(varDesc)));
            } else if (varDesc.is(VariableType.ARRAY))
                generateRHS(scope, node.jjtGetChild(1), statement.add(new AStore(varDesc)));
            statement.setType(varDesc);
        }
    }

    private void generateRHS(Scope scope, SimpleNode assignment, AddOperation addOperation) {
        if (assignment.is(JJTVARIABLE)) {
            String name = assignment.getTokenValue();
            VariableDesc varDesc = scope.getVariable(name);

            if (varDesc.isField()) {
                addOperation.add(new GetStatic(scope.getModuleName(), varDesc));
            } else if (varDesc.is(VariableType.SCALAR)) {
                addOperation.add(new ILoad(varDesc));
            } else {
                addOperation.add(new ALoad(scope.getVariable(assignment.getTokenValue())));
            }
        } else if (assignment.is(JJTARRAY)) {
            generateRHS(scope, assignment.jjtGetChild(0), addOperation.add(new NewArray()));
        } else if (assignment.is(JJTINTEGER)) {
            addOperation.add(new BiPush(assignment.getTokenValue()));
        } else if (assignment.is(JJTOPERATOR)) {
            addOperation = addOperation.add(new Operator(assignment.getTokenValue()));
            generateRHS(scope, assignment.jjtGetChild(0), addOperation);
            generateRHS(scope, assignment.jjtGetChild(1), addOperation);
        } else if (assignment.is(JJTNEGATION)) {
            generateRHS(scope, assignment.jjtGetChild(0), addOperation.add(new INeg()));
        } else if (assignment.is(JJTCALL)) {
            generateCall(scope, assignment, addOperation);
        } else if (assignment.is(JJTSIZEOF)) {
            addOperation.add(new ArrayLength())
                    .add(new ALoad(scope.getVariable(assignment.jjtGetChild(0).getTokenValue())));
        } else if (assignment.is(JJTARRAYACCESS)) {
            String name = assignment.getTokenValue();
            String index = assignment.jjtGetChild(0).getTokenValue();
            addOperation = addOperation.add(new IALoad());

            VariableDesc arrayDesc = scope.getVariable(name);
            if (arrayDesc.isField())
                addOperation.add(new GetStatic(scope.getModuleName(), arrayDesc));
            else
                addOperation.add(new ALoad(arrayDesc));

            if (Common.isInt(index))
                addOperation.add(new BiPush(index));
            else {
                VariableDesc varDesc = scope.getVariable(index);
                if (varDesc.isField()) {
                    addOperation.add(new GetStatic(scope.getModuleName(), varDesc));
                } else
                    addOperation.add(new ILoad(varDesc));
            }

        } else if (assignment.is(JJTSTRING)) {
            addOperation.add(new Ldc(assignment.getTokenValue()));
        }
    }

    private void loadArguments(Scope sope, SimpleNode node, AddOperation addOperation) {
        if (node.jjtGetNumChildren() > 0)
            for (Node n : node.getChildren()) {
                generateRHS(sope, (SimpleNode) n, addOperation);
            }
    }

    private void generateCall(Scope scope, SimpleNode node, AddOperation addOperation) {
        if (node.jjtGetChild(0).is(JJTMODULEACCESS)) {
            generateModuleCall(scope, node, addOperation);
            return;
        }
        String name = node.jjtGetChild(0).getTokenValue();
        FunctionDesc fnDesc = scope.getFunction(name);
        loadArguments(scope, node.jjtGetChild(1), addOperation.add(new InvokeStatic(scope.getModuleName(), fnDesc)));
    }

    private void generateModuleCall(Scope scope, SimpleNode node, AddOperation addOperation) {
        SimpleNode moduleAccessNode = node.jjtGetChild(0);
        String module = moduleAccessNode.getTokenValue();
        String name = moduleAccessNode.jjtGetChild(0).getTokenValue();

        List<String> args = new ArrayList<>();

        addOperation = addOperation.add(new InvokeStatic(module, name, args));

        SimpleNode argsNode = node.jjtGetChild(1);
        if (argsNode.jjtGetNumChildren() > 0)
            for (Node n : argsNode.getChildren()) {
                generateRHS(scope, (SimpleNode) n, addOperation);
                args.add(parseArg(scope, (SimpleNode) n));
            }

    }

    private String parseArg(Scope scope, SimpleNode node) {
        if (node.is(JJTSTRING))
            return "Ljava/lang/String;";
        if (node.is(JJTVARIABLE)) {
            String name = node.getTokenValue();
            if (scope.getVariable(name).is(VariableType.SCALAR))
                return "I";
            return "[I";
        }
        if (node.is(JJTINTEGER))
            return "I";
        return "";
    }
}
