package yal2jvm.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import yal2jvm.dataflow.algorithms.RegisterAlocationAlgorithm;
import yal2jvm.dataflow.algorithms.leftedge.LeftEdgeAlgorithm;
import yal2jvm.ir.Statement;
import yal2jvm.ir.operations.IStore;
import yal2jvm.ir.operations.Operation;
import yal2jvm.scope.VariableDesc;

public class RegisterAlocator {

    private final RegisterAlocationAlgorithm alocationAlgorithm = new LeftEdgeAlgorithm();
    private final List<Statement> statements;
    private final List<VariableDesc> localVariables;
    private final List<VariableDesc> parameters;

    public RegisterAlocator(List<Statement> statements, List<VariableDesc> parameters,
            List<VariableDesc> localVariables) {
        this.statements = statements;
        this.parameters = parameters;
        this.localVariables = localVariables;
    }

    public void basicAlocation() {
        AtomicInteger counter = new AtomicInteger(0);
        parameters.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
        localVariables.forEach(p -> p.setName(Integer.toString(counter.getAndIncrement())));
    }

    public void optimizedAlocation() {

        Collection<LivenessTableRow> livenessTable = new LivenessAnalysis(statements).analysise();

        // System.out.println(livenessTable);

        List<String> localVariablesNames = new ArrayList<>();
        List<String> parametersNames = new ArrayList<>();

        parameters.forEach(p -> {
            if (p.isReturnValue())
                localVariablesNames.add(p.getName());
            else
                parametersNames.add(p.getName());
        });

        localVariables.forEach(v -> {
            localVariablesNames.add(v.getName());
        });

        Map<String, Integer> registersMap = alocationAlgorithm.alocate(parametersNames, localVariablesNames,
                livenessTable);
        parameters.forEach(v -> {
            String varName = v.getName();
            if (registersMap.containsKey(varName))
                v.setName(Integer.toString(registersMap.get(varName)));
            else {
                removeUnused(v.getName());
            }
        });
        localVariables.forEach(v -> {
            String varName = v.getName();
            if (registersMap.containsKey(varName))
                v.setName(Integer.toString(registersMap.get(varName)));
            else {
                removeUnused(v.getName());
            }
        });
    }

    private void removeUnused(String unUsed) {
        for (Statement s : statements) {
            Operation operation = s.root.getOperation();
            if (operation.getClass().equals(IStore.class)) {
                if (IStore.class.cast(operation).getDesc().getName().equals(unUsed))
                    s.clear();
            }
        }
    }
}
