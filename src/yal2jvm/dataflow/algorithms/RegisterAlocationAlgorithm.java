package yal2jvm.dataflow.algorithms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yal2jvm.dataflow.LivenessTableRow;

public interface RegisterAlocationAlgorithm {
    Map<String, Integer> alocate(List<String> parameters, List<String> localVariables,
            Collection<LivenessTableRow> livenessTable);
}
