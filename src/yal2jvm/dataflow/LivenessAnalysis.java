package yal2jvm.dataflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import yal2jvm.ir.Statement;
import yal2jvm.ir.operations.ALoad;
import yal2jvm.ir.operations.AStore;
import yal2jvm.ir.operations.GoTo;
import yal2jvm.ir.operations.IInc;
import yal2jvm.ir.operations.ILoad;
import yal2jvm.ir.operations.IStore;
import yal2jvm.ir.operations.Label;
import yal2jvm.ir.operations.LowIrNode;
import yal2jvm.ir.operations.Operation;
import yal2jvm.scope.VariableDesc;

public class LivenessAnalysis {
    private final List<Statement> statements;

    public LivenessAnalysis(List<Statement> statements) {
        this.statements = statements;
    }

    public List<LivenessTableRow> analysise() {
        Collection<Visitor> visitors = createCFG();
        analysieLiveness(visitors);
        return visitors.stream().map(v -> v.convert()).collect(Collectors.toList());
    }

    private Collection<Visitor> createCFG() {
        Visitor current = null;
        Map<Statement, Visitor> visitors = new LinkedHashMap<>();

        Stack<Runnable> assignRef = new Stack<>();

        Iterator<Statement> stmsVisitor = statements.iterator();
        while (stmsVisitor.hasNext()) {
            Statement s = stmsVisitor.next();
            Visitor old = current;
            if (s.root.getOperation().getClass().equals(GoTo.class)) {
                Statement sRef = s.getRef();
                assignRef.push(() -> {
                    old.ref = visitors.get(sRef).next;
                });
                continue;
            }
            current = new Visitor(s);
            visitors.put(s, current);
            if (old != null)
                old.next = current;
        }
        // assigns refs
        while (!assignRef.isEmpty())
            assignRef.pop().run();

        List<Visitor> values = visitors.entrySet().stream()
                .filter(e -> !e.getKey().root.getOperation().getClass().equals(Label.class)).map(e -> e.getValue())
                .collect(Collectors.toList());

        Visitor oldV = null;

        int i = 0; // only for testing
        for (Visitor sv : values) {
            if (oldV != null)
                oldV.next = sv;
            sv.num = i++;
            oldV = sv;
        }

        values.forEach(v -> setDefAndUse(v, v.value.root));
        return values;
    }

    private void analysieLiveness(Collection<Visitor> visitors) {
        boolean continueLoop = true;
        do {
            continueLoop = false;
            for (Visitor v : visitors) {
                Set<VariableDesc> newIn = v.computeIn();
                Set<VariableDesc> newOut = v.computeOut();

                if (!v.in.equals(newIn) || !v.out.equals(newOut))
                    continueLoop = true;

                v.in = v.computeIn();
                v.out = v.computeOut();
            }
        } while (continueLoop);
    }

    private void setDefAndUse(Visitor visitor, LowIrNode node) {
        if (node == null)
            return;
        Operation op = node.getOperation();
        if (isDef(op))
            visitor.def.add(op.getDesc());
        if (isUse(op))
            visitor.use.add(op.getDesc());
        node.getChildren().forEach(ch -> setDefAndUse(visitor, ch));
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
