package yal2jvm.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import yal2jvm.dataflow.algorithms.LeftEdgeAlgorithm;
import yal2jvm.dataflow.algorithms.RegisterAlocationAlgorithm;
import yal2jvm.ir.Statement;
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
        List<VariableDesc> variables = new ArrayList<>();
        variables.addAll(localVariables);
        variables.addAll(parameters);
        Collection<LivenessTableRow> livenessTable = new LivenessAnalysis(statements).analysise();
        Map<String, Integer> registersMap = alocationAlgorithm.alocate(
                parameters.stream().map(v -> v.getName()).collect(Collectors.toList()),
                localVariables.stream().map(v -> v.getName()).collect(Collectors.toList()), livenessTable);
        parameters.forEach(v -> {
            String varName = v.getName();
            if (registersMap.containsKey(varName))
                v.setName(Integer.toString(registersMap.get(varName)));
            else {
                v.setName("ERROR");
            }
        });
        localVariables.forEach(v -> {
            String varName = v.getName();
            if (registersMap.containsKey(varName))
                v.setName(Integer.toString(registersMap.get(varName)));
            else {
                v.setName("ERROR");
            }
        });

    }
}
