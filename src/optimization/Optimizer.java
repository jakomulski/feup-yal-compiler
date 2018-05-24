package optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ir.Statement;
import ir.Constant;
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

    public void propagateConstants() {
        Map<String, Constant> declared = new HashMap<>();
        Set<String> used = new HashSet<>();

        Iterator<Statement> stmtIterator = statements.iterator();
        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            setConstant(s.root, declared);
            s.optimize();
            setUsage(s.root, used);
            setDeclared(s, declared, used);

            // System.out.println(s.root.getOperation());
            if (s.isIfElse()) {
                propagateConstantsForIfElse(stmtIterator, s.getIfEndLabel(), s.getElseEndLabel(), declared, used)
                        .forEach(e -> {
                            declared.remove(e);
                            used.remove(e);
                        });
            } else if (s.isIf()) {
                Optional<Boolean> checked = checkCondition(s);
                if (checked.isPresent()) {
                    if (!checked.get()) {
                        s.clear();
                        omit(stmtIterator, s.getIfEndLabel());
                        continue;
                    }
                }
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declared, used).forEach(e -> {
                    declared.remove(e);
                    used.remove(e);
                });
            } else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), declared, used).forEach(e -> {
                    declared.remove(e);
                    used.remove(e);
                });
            }
        }

        declared.forEach((k, v) -> {
            if (!used.contains(k)) {
                v.getStatement().clear();
            }
        });
    }

    private void omit(Iterator<Statement> stmtIterator, Statement endStatement) {
        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();
            s.clear();
            if (s.equals(endStatement)) {
                break;
            }
        }

    }

    private void setConstant(LowIrNode node, Map<String, Constant> declared) {
        if (node == null)
            return;
        Operation operation = node.getOperation();
        if (operation.getClass().equals(ILoad.class)) {
            String varName = operation.getDesc().getName();
            if (declared.containsKey(varName)) {
                LowIrNode container = operation.getContainer();
                container.clearChildren();
                container.setOperation(declared.get(varName).getOperation());

            }
        }
        node.getChildren().forEach(ch -> setConstant(ch, declared));
    }

    private void setDeclared(Statement s, Map<String, Constant> declared, Set<String> used) {
        Operation rootOperation = s.root.getOperation();
        if (rootOperation.getClass().equals(IStore.class)) {
            clearDeclaration(rootOperation, declared, used);
            if (s.root.getChildren().size() == 1) {
                Operation childOperation = s.root.getChildren().get(0).getOperation();
                if (childOperation.getClass().equals(IPush.class))
                    declared.put(rootOperation.getDesc().getName(), new Constant(s, childOperation));
                else {
                    declared.remove(rootOperation.getDesc().getName());
                }
            } else {
                declared.remove(rootOperation.getDesc().getName());
            }
        }
    }

    private void clearDeclaration(Operation rootOperation, Map<String, Constant> declared,
            Set<String> used) {
        String name = rootOperation.getDesc().getName();
        if (declared.containsKey(name) && !used.contains(name)) {
            declared.get(name).getStatement().clear();
        }

    }

    private void setUsage(LowIrNode node, Set<String> used) {
        if (node == null)
            return;
        Operation operation = node.getOperation();
        if (operation.getClass().equals(ILoad.class) || operation.getClass().equals(IInc.class)) {
            used.add(operation.getDesc().getName());
        }
        node.getChildren().forEach(ch -> setUsage(ch, used));

    }

    private Set<String> propagateConstantsForIfElse(Iterator<Statement> stmtIterator, Statement endIfStatement,
            Statement endElseStatement, Map<String, Constant> declared, Set<String> used) {
        Set<String> newlyDeclared = new HashSet<>();
        newlyDeclared.addAll(propagateConstantsInBlock(stmtIterator, endIfStatement, declared, used));
        newlyDeclared.addAll(propagateConstantsInBlock(stmtIterator, endElseStatement, declared, used));
        return newlyDeclared;
    }

    private Set<String> propagateConstantsInBlock(Iterator<Statement> stmtIterator, Statement endStatement,
            Map<String, Constant> declaredInParent, Set<String> usedInParent) {
        Set<String> redeclared = new HashSet<>();

        Map<String, Constant> newDeclared = new HashMap<>();
        Set<String> newUsed = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            setConstant(s.root, declaredInParent);
            setConstant(s.root, newDeclared);

            s.optimize();
            setUsage(s.root, usedInParent);
            setUsage(s.root, newUsed);
            setDeclared(s, newDeclared, newUsed);

            Operation rootOperation = s.root.getOperation();
            if (rootOperation.getClass().equals(IStore.class)) {
                if (declaredInParent.containsKey(rootOperation.getDesc().getName())) {
                    declaredInParent.remove(rootOperation.getDesc().getName());
                    redeclared.add(rootOperation.getDesc().getName());
                }
            }

            if (s.equals(endStatement)) {
                break;
            }

            if (s.isIfElse())
                propagateConstantsForIfElse(stmtIterator, s.getIfEndLabel(), s.getElseEndLabel(), declaredInParent,
                        usedInParent);
            else if (s.isIf())
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declaredInParent, usedInParent);
            else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), declaredInParent, usedInParent);
            }
        }
        newDeclared.forEach((k, v) -> {
            if (!newUsed.contains(k) && !declaredInParent.containsKey(k) && !redeclared.contains(k)) {
                declaredInParent.put(k, v);
            }
        });
        return redeclared;
    }

    private Set<String> propagateConstantsInLoopBlock(Iterator<Statement> stmtIterator, Statement endStatement,
            Map<String, Constant> declaredInParent, Set<String> usedInParent) {
        Set<String> redeclared = new HashSet<>();

        Map<String, Constant> newDeclared = new HashMap<>();
        Set<String> newUsed = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement s = stmtIterator.next();

            // Possible to do this: needs 2 iterations
            // setConstant(s.root, declared);
            setConstant(s.root, newDeclared);

            s.optimize();
            setUsage(s.root, usedInParent);
            setUsage(s.root, newUsed);
            setDeclared(s, newDeclared, newUsed);

            Operation rootOperation = s.root.getOperation();
            if (rootOperation.getClass().equals(IStore.class)) {
                declaredInParent.remove(rootOperation.getDesc().getName());
                redeclared.add(rootOperation.getDesc().getName());
            }

            if (s.equals(endStatement))
                break;

            if (s.isIfElse())
                propagateConstantsForIfElse(stmtIterator, s.getIfEndLabel(), s.getElseEndLabel(), declaredInParent,
                        usedInParent);
            else if (s.isIf())
                propagateConstantsInBlock(stmtIterator, s.getIfEndLabel(), declaredInParent, usedInParent);
            else if (s.isLoop()) {
                propagateConstantsInLoopBlock(stmtIterator, s.getLoopEndLabel(), declaredInParent, usedInParent);
            }
        }
        newDeclared.forEach((k, v) -> {
            if (!newUsed.contains(k) && !declaredInParent.containsKey(k) && !redeclared.contains(k)) {
                declaredInParent.put(k, v);
            }
        });
        return redeclared;
    }
}
