package yal2jvm.ir;

import static jjtree.Yal2jvmTreeConstants.JJTARRAY;
import static jjtree.Yal2jvmTreeConstants.JJTARRAYACCESS;
import static jjtree.Yal2jvmTreeConstants.JJTASSIGN;
import static jjtree.Yal2jvmTreeConstants.JJTCALL;
import static jjtree.Yal2jvmTreeConstants.JJTIF;
import static jjtree.Yal2jvmTreeConstants.JJTINTEGER;
import static jjtree.Yal2jvmTreeConstants.JJTMODULEACCESS;
import static jjtree.Yal2jvmTreeConstants.JJTNEGATION;
import static jjtree.Yal2jvmTreeConstants.JJTOPERATOR;
import static jjtree.Yal2jvmTreeConstants.JJTSIZEOF;
import static jjtree.Yal2jvmTreeConstants.JJTSTRING;
import static jjtree.Yal2jvmTreeConstants.JJTVARIABLE;
import static jjtree.Yal2jvmTreeConstants.JJTWHILE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jjtree.Node;
import jjtree.SimpleNode;
import yal2jvm.common.Constants;
import yal2jvm.common.StackSizeCounter;
import yal2jvm.dataflow.RegisterAlocator;
import yal2jvm.ir.operations.ALoad;
import yal2jvm.ir.operations.AReturn;
import yal2jvm.ir.operations.AStore;
import yal2jvm.ir.operations.AddOperation;
import yal2jvm.ir.operations.ArrayLength;
import yal2jvm.ir.operations.FillArray;
import yal2jvm.ir.operations.GetStatic;
import yal2jvm.ir.operations.GoTo;
import yal2jvm.ir.operations.IALoad;
import yal2jvm.ir.operations.IAStore;
import yal2jvm.ir.operations.ILoad;
import yal2jvm.ir.operations.INeg;
import yal2jvm.ir.operations.IPush;
import yal2jvm.ir.operations.IReturn;
import yal2jvm.ir.operations.IStore;
import yal2jvm.ir.operations.IfIcmp;
import yal2jvm.ir.operations.InvokeStatic;
import yal2jvm.ir.operations.Label;
import yal2jvm.ir.operations.Ldc;
import yal2jvm.ir.operations.NewArray;
import yal2jvm.ir.operations.Operator;
import yal2jvm.ir.operations.PutStatic;
import yal2jvm.ir.operations.Return;
import yal2jvm.optimization.ConstantsPropagator;
import yal2jvm.scope.FunctionDesc;
import yal2jvm.scope.Scope;
import yal2jvm.scope.VariableDesc;
import yal2jvm.scope.VariableType;
import yal2jvm.semantic.Common;

public class IrBuilder {
    private final StackSizeCounter stackSizeCounter;

    private final FunctionDesc functionDesc;
    private final List<Statement> statements = new ArrayList<>();

    private final List<VariableDesc> localVariables = new ArrayList<>();
    private final List<VariableDesc> parameters = new ArrayList<>();

    private CodeBuilder codeBuilder;

    public IrBuilder(FunctionDesc desc, CodeBuilder codeBuilder) {
        this.functionDesc = desc;
        this.codeBuilder = codeBuilder;
        stackSizeCounter = new StackSizeCounter();
    }

    public FunctionDesc getDescription() {
        return functionDesc;
    }

    public List<String> build() {
        statements.forEach(statement -> {
            generateStatement(statement);
        });

        if (Constants.OPTIMIZE) {
            new ConstantsPropagator(statements).propagateConstants();
        }

        if (Constants.GENERATE_LOCALS) {
            RegisterAlocator registerAlocator = new RegisterAlocator(statements, parameters, localVariables);
            if (Constants.OPTIMIZED_REGISTER_ALOCATION)
                registerAlocator.optimizedAlocation();
            else
                registerAlocator.basicAlocation();
        }

        List<Supplier<String>> lines = new ArrayList<>();
        generate(lines);
        return lines.stream().map(s -> s.get()).collect(Collectors.toList());
    }

    private void generate(List<Supplier<String>> supLines) {
        supLines.add(() -> ".method public static " + functionDesc.asJasmin());
        int numberOfLocals = parameters.size() + localVariables.size();

        supLines.add(() -> ".limit locals " + Integer.toString(numberOfLocals));
        supLines.add(() -> ".limit stack " + stackSizeCounter.getStackSize());
        statements.forEach(statement -> {
            if (!statement.isCleared()) {
                supLines.add(() -> statement.toString());
                statement.calculateStackSize(stackSizeCounter);
            }
        });
        supLines.add(() -> ".end method");
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
            if (!parameters.contains(desc))
                parameters.add(desc);
        } else {
            statement.add(new IReturn()).add(new ILoad(desc));
            if (!parameters.contains(desc))
                parameters.add(desc);
        }
    }

    public Statement addStatement(SimpleNode node, Scope scope) {
        Statement statement = new Statement(node, scope);
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
                addOperation.add(new IPush(index));
            else
                addOperation.add(new ILoad(scope.getVariable(index)));
            generateRHS(scope, node.jjtGetChild(1), addOperation);
        } else {
            VariableDesc varDesc = scope.getVariable(varName);
            if (varDesc.isFill()) {
                codeBuilder.generateFillArray();
                AddOperation addOp = statement.add(new FillArray(varDesc));
                addOp.add(new GetStatic(scope.getModuleName(), varDesc));
                generateRHS(scope, node.jjtGetChild(1), addOp);
            } else if (varDesc.isField()) {
                generateRHS(scope, node.jjtGetChild(1), statement.add(new PutStatic(scope.getModuleName(), varDesc)));
            } else if (varDesc.is(VariableType.SCALAR) || varDesc.is(VariableType.ANY)) {
                generateRHS(scope, node.jjtGetChild(1), statement.add(new IStore(varDesc)));
            } else if (varDesc.is(VariableType.ARRAY)) {
                generateRHS(scope, node.jjtGetChild(1), statement.add(new AStore(varDesc)));
            }
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
            addOperation.add(new IPush(assignment.getTokenValue()));
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
                addOperation.add(new IPush(index));
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
