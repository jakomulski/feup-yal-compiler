package semantic;

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
import static yal2jvm.Yal2jvmTreeConstants.JJTSTRING;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTWHILE;

import custom.Logger;
import ir.IrBuilder;
import ir.Statement;
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
    private final IrBuilder irBuilder;

    public StatementsAnalyzer(IrBuilder irBuilder) {
        this.irBuilder = irBuilder;
    }

    public void analyzeStatements(SimpleNode node, Scope scope) {
        if (node.jjtGetNumChildren() == 0)
            return;
        for (Node n : node.getChildren()) {
            SimpleNode statement = (SimpleNode) n;

            if (statement.is(JJTCALL)) {
                analyzeCall(statement, scope);
                irBuilder.addStatement(statement, scope);
            } else if (statement.is(JJTASSIGN)) {
                analyzeAssign(statement, scope);
                irBuilder.addStatement(statement, scope);
            } else if (statement.is(JJTWHILE)) {
                Statement loopLabel = new Statement();
                Statement endLoopLabel = new Statement();
                Statement goToLoop = new Statement().setRef(loopLabel);

                irBuilder.addLabelStatement(loopLabel);

                irBuilder.addStatement(statement, scope).setRef(endLoopLabel);

                SimpleNode condition = statement.jjtGetChild(0);
                SimpleNode lhs = condition.jjtGetChild(0);
                SimpleNode rhs = condition.jjtGetChild(1);

                checkRhs(lhs, scope, VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false));
                checkRhs(rhs, scope, VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false));

                SimpleNode statements = statement.jjtGetChild(1);
                analyzeStatements(statements, ScopeFactory.INSTANCE.createBlockedScope(scope));

                irBuilder.addGoToStatement(goToLoop);
                irBuilder.addLabelStatement(endLoopLabel);

            } else if (statement.is(JJTIF)) {

                Statement endIfLabel = new Statement();
                Statement endElseLabel = new Statement();
                Statement goToEndElse = new Statement().setRef(endElseLabel);

                irBuilder.addStatement(statement, scope).setRef(endIfLabel);

                SimpleNode condition = statement.jjtGetChild(0);
                SimpleNode lhs = condition.jjtGetChild(0);
                SimpleNode rhs = condition.jjtGetChild(1);

                checkRhs(lhs, scope, VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false));
                checkRhs(rhs, scope, VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false));

                BlockedSimpleScope ifScope = ScopeFactory.INSTANCE.createBlockedScope(scope);

                BlockedSimpleScope elseScope = ScopeFactory.INSTANCE.createBlockedScope(scope);

                SimpleNode ifStatements = statement.jjtGetChild(1);
                analyzeStatements(ifStatements, ifScope);

                if (statement.jjtGetNumChildren() == 3) {
                    irBuilder.addGoToStatement(goToEndElse);
                    irBuilder.addLabelStatement(endIfLabel);
                    SimpleNode elseStatements = statement.jjtGetChild(2);
                    analyzeStatements(elseStatements, elseScope);

                    irBuilder.addLabelStatement(endElseLabel);
                } else
                    irBuilder.addLabelStatement(endIfLabel);
                scope.mergeInitialized(ifScope, elseScope);
            }
        }

    }

    private VariableType analyzeCall(SimpleNode node, Scope scope) {
        SimpleNode nameNode = node.jjtGetChild(0);
        SimpleNode argumentsNode = node.jjtGetChild(1);

        if (nameNode.is(JJTMODULEACCESS)) {
            LOGGER.semanticInfo(node,
                    "call " + nameNode.getTokenValue() + "." + nameNode.jjtGetChild(0).getTokenValue());

            for (int i = 0; i < argumentsNode.jjtGetNumChildren(); ++i) {
                checkRhs(argumentsNode.jjtGetChild(i), scope,
                        VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ANY, false));
            }

            return VariableType.ANY; // CANNOT BE CHECKED
        }

        String name = nameNode.getTokenValue();

        if (!scope.hasFunction(name)) {
            LOGGER.semanticError(node, "missing funtion");
            return VariableType.NULL;
        }

        FunctionDesc desc = scope.getFunction(name);

        if (desc.getParamsNum() != argumentsNode.jjtGetNumChildren()) {
            LOGGER.semanticError(node, "incorrect number of parameters");
        } else {
            for (int i = 0; i < desc.getParamsNum(); ++i) {
                checkArgument(argumentsNode.jjtGetChild(i), scope, desc.getArumentsTypes().get(i));
            }
        }

        return desc.getReturnType();
    }

    private void checkArgument(SimpleNode var, Scope scope, VariableType paramDesc) {
        String name = var.getTokenValue();
        if (var.is(JJTINTEGER)) {
            if (!paramDesc.equals(VariableType.SCALAR))
                LOGGER.semanticError(var, "incorrect type");
        } else if (var.is(JJTSTRING)) {
            LOGGER.semanticError(var, "string is unsupported");
        } else if (!Common.checkUndeclaredAndUninitialized(scope, var))

        {
        } else if (!scope.getVariable(name).getType().equals(paramDesc)) {
            LOGGER.semanticError(var, "incorrect type");
        }
    }

    private void analyzeAssign(SimpleNode node, Scope scope) {
        SimpleNode access = node.jjtGetChild(0);
        VariableDesc desc = null;
        String name = access.getTokenValue();

        if (access.is(JJTVARIABLE)) {
            if (!scope.hasVariable(name)) {
                VariableDesc newDesc = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ANY, false);
                scope.addVariable(name, newDesc);
                irBuilder.addVariable(newDesc);
            }
            desc = scope.getVariable(name);
            LOGGER.semanticInfo(access, "set " + name);

        } else if (access.is(JJTARRAYACCESS)) {
            // TODO: check the type
            checkArrayAccess(access, scope);
            desc = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, true);
        }

        checkRhs(node.jjtGetChild(1), scope, desc);
    }

    private void checkArrayAccess(SimpleNode node, Scope scope) {
        String name = node.getTokenValue();
        if (!Common.checkUndeclaredAndUninitialized(scope, node)) {
        } else {
            String indexValue = node.jjtGetChild(0).getTokenValue();
            if (!Common.isInt(indexValue)) {
                if (!Common.checkUndeclaredAndUninitialized(scope, node.jjtGetChild(0))) {
                } else if (scope.getVariable(indexValue).is(scope.getVariable(name).getType()))
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
            SimpleNode nameNode = assignment.jjtGetChild(0);

            if (nameNode.is(JJTINTEGER)) {
                desc.initialize();
                int size = Integer.parseInt(assignment.jjtGetChild(0).getTokenValue());
                desc.setType(VariableType.ARRAY);

                LOGGER.semanticInfo(assignment, "load " + size);
            } else if (nameNode.is(JJTSIZEOF)) {
                if (Common.checkSizeOf(nameNode, scope))
                    desc.initialize();
            } else {
                String varName = nameNode.getTokenValue();
                if (!scope.hasVariable(varName)) {
                    LOGGER.semanticError(assignment, "undeclared" + varName);
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
        // SimpleNode access = node.jjtGetChild(0);

        if (assignment.is(JJTARRAY)) {
            checkRhsArrayAssign(assignment, scope, desc);
        } else if (assignment.is(JJTCALL)) {
            VariableType returnType = analyzeCall(assignment, scope);
            if (returnType.equals(VariableType.NULL)) {
                LOGGER.semanticError(assignment, "function do not return any value");
            } else if (desc.is(VariableType.ANY)) {
                desc.setType(returnType);
            } else if (returnType.equals(VariableType.ANY)) {
                returnType = desc.getType();
            } else if (!desc.is(returnType)) {
                LOGGER.semanticError(assignment, "incorrect type " + desc.getType() + " " + returnType);
            }

        } else if (assignment.is(JJTVARIABLE)) {
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
        } else if (desc.is(VariableType.ARRAY)) {
            LOGGER.semanticError(assignment, "incorrect type");
        } else if (assignment.is(JJTINTEGER)) {
            if (desc.is(VariableType.ANY))
                desc.setType(VariableType.SCALAR);
            LOGGER.semanticInfo(assignment, "load " + assignment.getTokenValue());
        } else if (assignment.is(JJTOPERATOR)) {
            if (desc.is(VariableType.ANY))
                desc.setType(VariableType.SCALAR);
            LOGGER.semanticInfo(assignment, assignment.getTokenValue());
            checkRhs(assignment.jjtGetChild(0), scope, desc);
            checkRhs(assignment.jjtGetChild(1), scope, desc);
        } else if (assignment.is(JJTNEGATION)) {
            if (desc.is(VariableType.ANY))
                desc.setType(VariableType.SCALAR);
            LOGGER.semanticInfo(assignment, "negation");
            checkRhs(assignment.jjtGetChild(0), scope, desc);
        } else if (assignment.is(JJTSIZEOF)) {
            checkSizeOf(assignment, scope);
        } else if (assignment.is(JJTARRAYACCESS)) {
            if (desc.is(VariableType.ANY))
                desc.setType(VariableType.SCALAR);
            checkArrayAccess(assignment, scope);
        } else if (assignment.is(JJTMODULEACCESS)) {
            LOGGER.semanticInfo(assignment,
                    "load " + assignment.getTokenValue() + "." + assignment.jjtGetChild(0).getTokenValue());
        } else if (assignment.is(JJTSTRING)) {
            // DO NOTHING
        }

        desc.initialize();
    }
}
