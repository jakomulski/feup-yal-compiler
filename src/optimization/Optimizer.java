package optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ir.Constant;
import ir.Statement;
import operations.IInc;
import operations.ILoad;
import operations.IPush;
import operations.IStore;
import operations.IfIcmp;
import operations.LowIrNode;
import operations.Operation;

public class Optimizer {

    private final List<Statement> statements;

    public Optimizer(List<Statement> statements) {
        this.statements = statements;
    }

    private Optional<Boolean> checkCondition(Statement s) {
        Operation ifIcmp = s.root.getOperation();

        if (ifIcmp.getClass().equals(IfIcmp.class)) {
            Operation firstCondition = s.root.getChildren().get(0).getOperation();
            Operation secondCondition = s.root.getChildren().get(1).getOperation();
            if (firstCondition.getClass().equals(IPush.class) && secondCondition.getClass().equals(IPush.class)) {
                boolean condition = IfIcmp.class.cast(ifIcmp).checkCondition(
                        IPush.class.cast(firstCondition).getIValue(), IPush.class.cast(secondCondition).getIValue());
                return Optional.of(condition);
            }
        }
        return Optional.empty();
    }

    private Set<String> globalyUsed = new HashSet<>();

    public void propagateConstants() {
        Map<String, Constant> declared = new HashMap<>();
        Set<String> used = new HashSet<>();

        Iterator<Statement> stmtIterator = statements.iterator();
        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            setConstant(s.root, declared);
            s.optimize();
            setUsage(s.root, used);
            addDeclarationAndClearPrevious(s, declared, used);

            if (s.isIfElse()) {
                propagateConstantsForIfElse(checkCondition(s), stmtIterator, declared, used, s);
            } else if (s.isIf()) {
                Optional<Boolean> checked = checkCondition(s);
                if (checked.isPresent()) {
                    if (!checked.get()) {
                        s.clear();
                        omit(stmtIterator, s.getIfEndLabel());
                        continue;
                    } else {
                        s.getIfEndLabel().clear();
                        s.clear();
                    }
                }
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declared, used);
            } else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), declared, used);
            }
        }

        statements.forEach(s -> {
            Operation rootOperation = s.root.getOperation();
            if (rootOperation.getClass().equals(IStore.class)) {
                String name = rootOperation.getDesc().getName();
                if (!globalyUsed.contains(name)) {
                    s.clear();
                }
            }
        });
    }

    private void omit(Iterator<Statement> stmtIterator, Statement endStatement) {
        /**
         * Clear block of statements
         */
        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();
            s.clear();
            if (s.equals(endStatement)) {
                break;
            }
        }

    }

    private void setConstant(LowIrNode node, Map<String, Constant> declared) {
        /**
         * Propagate constants
         */
        if (node == null)
            return;
        Operation operation = node.getOperation();
        if (operation.getClass().equals(ILoad.class)) {
            String varName = operation.getDesc().getName();
            if (declared.containsKey(varName) && declared.get(varName).isConstant()) {
                LowIrNode container = operation.getContainer();
                container.clearChildren();
                container.setOperation(declared.get(varName).getOperation());
            }
        }
        node.getChildren().forEach(ch -> setConstant(ch, declared));
    }

    private void addDeclarationAndClearPrevious(Statement s, Map<String, Constant> declared, Set<String> used) {
        /**
         * Adds declaration to @param declared and clears previous declaration
         * if not used
         */
        Operation rootOperation = s.root.getOperation();
        if (rootOperation.getClass().equals(IStore.class)) {
            clearDeclaration(rootOperation, declared, used);
            if (s.root.getChildren().size() == 1) {
                Operation childOperation = s.root.getChildren().get(0).getOperation();
                if (childOperation.getClass().equals(IPush.class)) {
                    declared.put(rootOperation.getDesc().getName(), new Constant(s, childOperation));
                    return;
                }
            }
            declared.put(rootOperation.getDesc().getName(), new Constant(s));
        }
    }

    private void addVariable(Statement s, Map<String, Constant> declared, Set<String> used) {
        /**
         * Adds variable to @param declared
         */
        Operation rootOperation = s.root.getOperation();
        if (rootOperation.getClass().equals(IStore.class)) {
            declared.put(rootOperation.getDesc().getName(), new Constant(s));
        }
    }

    private void clearDeclaration(Operation rootOperation, Map<String, Constant> declared, Set<String> used) {
        /**
         * Clear statement when is not used
         */
        String name = rootOperation.getDesc().getName();
        if (declared.containsKey(name) && declared.get(name).isConstant() && !used.contains(name)) {
            declared.get(name).getStatement().clear();
        }
    }

    private void setUsage(LowIrNode node, Set<String> used) {
        /**
         * Adds to used when variable is used
         */
        if (node == null)
            return;
        Operation operation = node.getOperation();
        if (operation.getClass().equals(ILoad.class) || operation.getClass().equals(IInc.class)) {
            String name = operation.getDesc().getName();
            used.add(name);
            this.globalyUsed.add(name);
        }
        node.getChildren().forEach(ch -> setUsage(ch, used));
    }

    private void propagateConstantsForIfElse(Optional<Boolean> checked, Iterator<Statement> stmtIterator,
            Map<String, Constant> declared, Set<String> used, Statement s) {
        Statement endIfStatement = s.getIfEndLabel();
        Statement endElseStatement = s.getElseEndLabel();
        if (checked.isPresent()) {
            s.clear();
            if (checked.get()) {
                propagateConstantsInBlock(stmtIterator, endIfStatement, declared, used);
                omit(stmtIterator, endElseStatement);
            } else {
                omit(stmtIterator, endIfStatement);
                propagateConstantsInBlock(stmtIterator, endElseStatement, declared, used);
            }
            s.getIfEndLabel().clear();
            s.getElseEndLabel().clear();
            s.getGoToEndElseStatement().clear();
            return;
        }

        propagateConstantsInBlock(stmtIterator, endIfStatement, declared, used);
        propagateConstantsInBlock(stmtIterator, endElseStatement, declared, used);
    }

    private void propagateConstantsInBlock(Iterator<Statement> stmtIterator, Statement endStatement,
            Map<String, Constant> declaredInParent, Set<String> usedInParent) {

        Map<String, Constant> newDeclared = new HashMap<>();
        Set<String> newUsed = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            setConstant(s.root, declaredInParent);
            setConstant(s.root, newDeclared);

            s.optimize();
            setUsage(s.root, usedInParent);
            setUsage(s.root, newUsed);

            addDeclarationAndClearPrevious(s, newDeclared, newUsed);

            if (s.equals(endStatement)) {
                break;
            }

            if (s.isIfElse())
                propagateConstantsForIfElse(checkCondition(s), stmtIterator, newDeclared, usedInParent, s);
            else if (s.isIf()) {
                Optional<Boolean> checked = checkCondition(s);
                if (checked.isPresent()) {
                    if (!checked.get()) {
                        s.clear();
                        omit(stmtIterator, s.getIfEndLabel());
                        continue;
                    } else {
                        s.getIfEndLabel().clear();
                        s.clear();
                    }
                }
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), newDeclared, usedInParent);
            } else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), newDeclared, usedInParent);
            }

            newDeclared.forEach((k, v) -> {
                declaredInParent.put(k, new Constant(v.getStatement()));
            });
            // addVariable(s, declaredInParent, usedInParent);
        }

    }

    private void propagateConstantsInLoopBlock(Iterator<Statement> stmtIterator, Statement endStatement,
            Map<String, Constant> declaredInParent, Set<String> usedInParent) {
        Map<String, Constant> newDeclared = new HashMap<>();
        Set<String> newUsed = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            // setConstant(s.root, declaredInParent);
            setConstant(s.root, newDeclared);

            s.optimize();
            setUsage(s.root, usedInParent);
            setUsage(s.root, newUsed);

            // addDeclarationAndClearPrevious(s, newDeclared, newUsed);
            addVariable(s, declaredInParent, usedInParent);

            if (s.equals(endStatement)) {
                break;
            }

            if (s.isIfElse())
                propagateConstantsForIfElse(checkCondition(s), stmtIterator, declaredInParent, usedInParent, s);
            else if (s.isIf())
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declaredInParent, usedInParent);
            else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), declaredInParent, usedInParent);
            }
        }
    }
}
