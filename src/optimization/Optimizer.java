package optimization;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ir.Statement;
import operations.ALoad;
import operations.AStore;
import operations.IInc;
import operations.ILoad;
import operations.IStore;
import operations.LowIrNode;
import operations.Operation;
import scope.VariableDesc;

public class Optimizer {

    public void optimize(List<Statement> statements) {
        Collection<Visitor<Statement>> visitors = create(statements);
        visit(visitors);

    }

    private boolean isNeedless(Statement statement) {
        Operation operation = statement.root.getOperation();

        return operation.getClass().equals(operations.Label.class)
                || operation.getClass().equals(operations.GoTo.class);
    }

    private Collection<Visitor<Statement>> create(List<Statement> statements) {
        Visitor<Statement> first = null;
        Visitor<Statement> current = null;
        Map<Statement, Visitor<Statement>> visitors = new LinkedHashMap<>();
        int i = 0;
        for (Statement s : statements) {
            // if(isNeedless(s))
            // continue;
            Visitor<Statement> old = current;
            current = new Visitor<>(s);
            current.num = ++i;
            visitors.put(s, current);
            if (old != null) {
                old.next = current;
            } else {
                first = current;
            }
        }
        visitors.get(statements.get(statements.size() - 1)).next = first;
        visitors.values().forEach(v -> {
            if (v.value.getRef() != null && v.value.getRef() != v.value)
                v.ref = visitors.get(v.value.getRef());
            setLists(v);
        });
        return visitors.values();
    }

    AtomicInteger counter = new AtomicInteger(0);

    private void visit(Collection<Visitor<Statement>> visitors) {
        boolean continueLoop = true;
        int num = 0;
        do {
            num++;
            continueLoop = false;
            for (Visitor<Statement> v : visitors) {
                Set<VariableDesc> newIn = v.computeIn();
                Set<VariableDesc> newOut = v.computeOut();

                if (!v.in.equals(newIn) || !v.out.equals(newOut))
                    continueLoop = true;

                v.in = v.computeIn();
                v.out = v.computeOut();
            }
        } while (continueLoop);

        // visitors.forEach(
        // v -> System.out.println(v.num + ": " + printVarDescList(v.in) + " " +
        // printVarDescList(v.out)));

    }

    private void visit(Visitor<Statement> visitor) {

        visitor.in = visitor.computeIn();
        visitor.out = visitor.computeOut();

        // System.out.println(visitor.num + ": " + printVarDescList(visitor.in)
        // + " " + printVarDescList(visitor.out));

        if (counter.incrementAndGet() == 100)
            return;
        if (visitor.next != null)
            visit(visitor.next);
    }

    private String printVarDescList(Set<VariableDesc> list) {
        return list.stream().map(e -> e.getName()).collect(Collectors.toSet()).toString();
    }

    private void setLists(Visitor<Statement> visitor) {
        visit(visitor, visitor.value.root);
    }

    private void visit(Visitor<Statement> visitor, LowIrNode node) {
        if (node == null)
            return;
        Operation op = node.getOperation();
        if (isDef(op))
            visitor.def.add(op.getDesc());
        if (isUse(op))
            visitor.use.add(op.getDesc());
        node.getChildren().forEach(ch -> visit(visitor, ch));
    }

    private boolean isDef(Operation operation) {
        Class<?> clazz = operation.getClass();
        return clazz.equals(AStore.class) || clazz.equals(IInc.class) || clazz.equals(IStore.class);
    }

    private boolean isUse(Operation operation) {
        Class<?> clazz = operation.getClass();
        return clazz.equals(ALoad.class) || clazz.equals(IInc.class) || clazz.equals(ILoad.class);
    }

}

class Visitor<T> {
    Visitor(T value) {
        this.value = value;
    }

    int num;
    T value;
    Visitor<T> next;
    Visitor<T> ref;

    Set<VariableDesc> in = new HashSet<>();
    Set<VariableDesc> out = new HashSet<>();

    Set<VariableDesc> def = new HashSet<>();
    Set<VariableDesc> use = new HashSet<>();

    Set<VariableDesc> computeOut() {
        Set<VariableDesc> out = new HashSet<>();
        if (next != null)
            out.addAll(next.in);
        if (ref != null)
            out.addAll(ref.in);
        return out;
    }

    Set<VariableDesc> computeIn() {
        Set<VariableDesc> in = new HashSet<>();
        in.addAll(this.use);
        in.addAll(out.stream().filter(e -> !def.contains(e)).collect(Collectors.toSet()));
        return in;
    }

}
