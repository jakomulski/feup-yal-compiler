package yal2jvm.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import yal2jvm.common.StatementsIterator;
import yal2jvm.ir.Statement;
import yal2jvm.ir.operations.ALoad;
import yal2jvm.ir.operations.AStore;
import yal2jvm.ir.operations.IInc;
import yal2jvm.ir.operations.ILoad;
import yal2jvm.ir.operations.IPush;
import yal2jvm.ir.operations.IStore;
import yal2jvm.ir.operations.IfIcmp;
import yal2jvm.ir.operations.LowIrNode;
import yal2jvm.ir.operations.Operation;

public class ConstantsPropagator {

    private final List<Statement> statements;

    public ConstantsPropagator(List<Statement> statements) {
        this.statements = statements;
    }

    private Set<String> globalyUsed = new HashSet<>();

    public Set<String> propagateConstants() {
        Map<String, Constant> declared = new HashMap<>();
        Set<String> used = new HashSet<>();

        Iterator<Statement> stmtIterator = new StatementsIterator(statements);
        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();
            setConstant(s.root, declared);
            s.optimize();
            setUsage(s.root, used);
            addDeclarationAndClearPrevious(s, declared, used);

            if (s.isIfElse()) {
                propagateConstantsForIfElse(checkCondition(s, declared), stmtIterator, declared, used, s)
                        .forEach((k, v) -> {
                            declared.put(k, new Constant(v.getStatement()));
                        });
            } else if (s.isIf()) {
                Optional<Boolean> checked = checkCondition(s, declared);
                if (checked.isPresent()) {
                    if (!checked.get()) {
                        s.clear();
                        omit(stmtIterator, s.getIfEndLabel());
                        continue;
                    } else {
                        s.getIfEndLabel().clear();
                        s.clear();
                        continue;
                    }
                }
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declared, used).forEach((k, v) -> {
                    declared.put(k, new Constant(v.getStatement()));
                });
            } else if (s.isLoop()) {
                Optional<Boolean> checked = checkCondition(s.getLoopCondition(), declared);
                if (checked.isPresent()) {
                    if (!checked.get()) { // Omit while block
                        s.clear();
                        omit(stmtIterator, s.getLoopEndLabel());
                        continue;
                    }
                }
                propagateConstantsInLoopBlock(stmtIterator, new HashMap<>(declared), used, s).forEach((k, v) -> {
                    declared.put(k, new Constant(v.getStatement()));
                });
            }
        }

        statements.forEach(s -> {
            Operation rootOperation = s.root.getOperation();
            if (rootOperation.getClass().equals(IStore.class) || rootOperation.getClass().equals(AStore.class)) {
                String name = rootOperation.getDesc().getName();
                if (!globalyUsed.contains(name)) {
                    s.clear();
                }
            }
        });
        return globalyUsed;
    }

    private Map<String, Constant> propagateConstantsInBlock(Iterator<Statement> stmtIterator, Statement endStatement,
            Map<String, Constant> declaredInParent, Set<String> usedInParent) {

        Map<String, Constant> newDeclared = new HashMap<>();
        Set<String> newUsed = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();
            // setConstant(s.root, declaredInParent);
            setConstant(s.root, union(declaredInParent, newDeclared));
            s.optimize();
            setUsage(s.root, usedInParent);
            setUsage(s.root, newUsed);

            addDeclarationAndClearPrevious(s, newDeclared, newUsed);

            if (s.equals(endStatement)) {
                break;
            }

            if (s.isIfElse())
                propagateConstantsForIfElse(checkCondition(s, union(newDeclared, declaredInParent)), stmtIterator,
                        union(newDeclared, declaredInParent), usedInParent, s).forEach((k, v) -> {
                            newDeclared.put(k, new Constant(v.getStatement()));
                        });
            else if (s.isIf()) {
                Optional<Boolean> checked = checkCondition(s, union(newDeclared, declaredInParent));
                if (checked.isPresent()) {
                    if (!checked.get()) {
                        s.clear();
                        omit(stmtIterator, s.getIfEndLabel());
                        continue; // Do not threat is as if
                    } else {
                        s.getIfEndLabel().clear();
                        s.clear();
                        continue;
                    }
                }
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), union(newDeclared, declaredInParent),
                        usedInParent).forEach((k, v) -> {
                            newDeclared.put(k, new Constant(v.getStatement()));
                        });
            } else if (s.isLoop()) {
                Optional<Boolean> checked = checkCondition(s.getLoopCondition(), union(newDeclared, declaredInParent));
                if (checked.isPresent()) {
                    if (!checked.get()) { // Omit while block
                        s.clear();
                        omit(stmtIterator, s.getLoopEndLabel());
                        continue;
                    }
                }
                propagateConstantsInLoopBlock(stmtIterator, union(newDeclared, declaredInParent), usedInParent, s)
                        .forEach((k, v) -> {
                            newDeclared.put(k, new Constant(v.getStatement()));
                        });
            }
        }
        return newDeclared;
    }

    private Map<String, Constant> propagateConstantsForIfElse(Optional<Boolean> checked,
            Iterator<Statement> stmtIterator, Map<String, Constant> declared, Set<String> used, Statement s) {
        Statement endIfStatement = s.getIfEndLabel();
        Statement endElseStatement = s.getElseEndLabel();
        Map<String, Constant> newDeclared = new HashMap<>();
        if (checked.isPresent()) {
            s.clear();
            if (checked.get())
                findAndOmit(endIfStatement, endElseStatement);
            else
                omit(stmtIterator, endIfStatement);

            s.getIfEndLabel().clear();
            s.getElseEndLabel().clear();
            s.getGoToEndElseStatement().clear();
            return newDeclared;
        }

        Map<String, Constant> ifDeclared = propagateConstantsInBlock(stmtIterator, endIfStatement, declared, used);
        Map<String, Constant> elseDeclared = propagateConstantsInBlock(stmtIterator, endElseStatement, declared, used);

        ifDeclared.forEach((k, v) -> {
            newDeclared.put(k, new Constant(v.getStatement()));
        });
        elseDeclared.forEach((k, v) -> {
            newDeclared.put(k, new Constant(v.getStatement()));
        });
        return newDeclared;
    }

    private Map<String, Constant> propagateConstantsInLoopBlock(Iterator<Statement> stmtIterator,
            Map<String, Constant> declared, Set<String> used, Statement s) {
        precheckLoop(s.getLoopCondition(), s.getLoopEndLabel(), declared, used);
        return propagateConstantsInBlock(stmtIterator, s.getLoopEndLabel(), declared, used);
    }

    private void precheckLoop(Statement startStatement, Statement endStatement, Map<String, Constant> declared,
            Set<String> used) {
        Iterator<Statement> stmtIterator = statements.iterator();

        // find start statement
        Statement s = null;
        while (stmtIterator.hasNext()) {
            s = stmtIterator.next();
            if (s.equals(startStatement))
                break;
        }

        // remove declarations if are redeclared
        while (stmtIterator.hasNext()) {
            s = stmtIterator.next();
            Operation rootOperation = s.root.getOperation();
            if (rootOperation.getClass().equals(IStore.class) || rootOperation.getClass().equals(AStore.class)
                    || rootOperation.getClass().equals(IInc.class)) {
                String name = rootOperation.getDesc().getName();
                declared.remove(name);
            }

            if (s.equals(endStatement))
                break;
        }
    }

    private Optional<Boolean> checkCondition(Statement s, Map<String, Constant> declared) {
        Operation ifIcmp = s.root.getOperation();

        if (ifIcmp.getClass().equals(IfIcmp.class)) {
            Operation firstCondition = s.root.getChildren().get(0).getOperation();
            Operation secondCondition = s.root.getChildren().get(1).getOperation();
            firstCondition = propagateConditionOperation(firstCondition, declared);
            secondCondition = propagateConditionOperation(secondCondition, declared);
            if (firstCondition.getClass().equals(IPush.class) && secondCondition.getClass().equals(IPush.class)) {
                boolean condition = IfIcmp.class.cast(ifIcmp).checkCondition(
                        IPush.class.cast(firstCondition).getIValue(), IPush.class.cast(secondCondition).getIValue());
                return Optional.of(condition);
            }
        }
        return Optional.empty();
    }

    private Operation propagateConditionOperation(Operation condition, Map<String, Constant> declared) {
        if (condition.getClass().equals(ILoad.class)) {
            String varName = condition.getDesc().getName();
            if (declared.containsKey(varName) && declared.get(varName).isConstant()) {
                return declared.get(varName).getOperation();
            }

        }
        return condition;
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

    private void findAndOmit(Statement startStatement, Statement endStatement) {
        /**
         * Clear block of statements
         */
        Iterator<Statement> stmtIterator = statements.iterator();

        // find start statement
        Statement s = null;
        while (stmtIterator.hasNext()) {
            s = stmtIterator.next();
            if (s.equals(startStatement))
                break;
        }
        if (s != null)
            s.clear();
        // remove block of code
        while (stmtIterator.hasNext()) {
            s = stmtIterator.next();
            s.clear();
            // it is not possible: stmtIterator.remove();
            if (s.equals(endStatement))
                break;
        }
    }

    private void setConstant(LowIrNode node, Map<String, Constant> declared) {
        /**
         * Propagate constants
         */
        if (node == null)
            return;
        Operation operation = node.getOperation();
        if (operation.getClass().equals(ILoad.class) || operation.getClass().equals(ALoad.class)) {
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
        if (rootOperation.getClass().equals(IStore.class) || rootOperation.getClass().equals(AStore.class)
                || rootOperation.getClass().equals(IInc.class)) {
            clearDeclaration(rootOperation, declared, used);
            if (s.root.getChildren().size() == 1) {
                Operation childOperation = s.root.getChildren().get(0).getOperation();
                // we can not assign calls, it would result in calling two times
                // the same function
                // Get static is also not the best idea <= it would require more
                // complex analysis and makes no sense (multithreading)
                if (childOperation.getClass().equals(IPush.class) || childOperation.getClass().equals(ILoad.class)
                        || childOperation.getClass().equals(ALoad.class)) {
                    declared.put(rootOperation.getDesc().getName(), new Constant(s, childOperation));
                    return;
                }
            }
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
        if (operation.getClass().equals(ALoad.class) || operation.getClass().equals(ILoad.class)
                || operation.getClass().equals(IInc.class)) {
            String name = operation.getDesc().getName();
            used.add(name);
            this.globalyUsed.add(name);
        }
        node.getChildren().forEach(ch -> setUsage(ch, used));
    }

    private Map<String, Constant> union(Map<String, Constant> first, Map<String, Constant> second) {
        Map<String, Constant> union = new HashMap<>(first);
        union.putAll(second);
        return union;
    }

}
