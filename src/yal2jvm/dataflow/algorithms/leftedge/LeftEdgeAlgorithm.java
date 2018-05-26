package yal2jvm.dataflow.algorithms.leftedge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yal2jvm.dataflow.LivenessTableRow;
import yal2jvm.dataflow.algorithms.RegisterAlocationAlgorithm;

public class LeftEdgeAlgorithm implements RegisterAlocationAlgorithm {

    @Override
    public Map<String, Integer> alocate(List<String> parameters, List<String> localVariables,
            Collection<LivenessTableRow> livenessTable) {
        Map<String, Integer> alocationMap = new HashMap<>();
        // System.out.println(livenessTable);
        List<LiveRange> variables = getLiveRanges(livenessTable);

        // Sort by the start time
        variables.sort((f, s) -> f.getStart().compareTo(s.getStart()));

        // Add variables to segments - left edge algorithm
        List<Segment> segments = new ArrayList<>();
        while (!variables.isEmpty()) {
            Segment newSegment = new Segment();
            Iterator<LiveRange> varIterator = variables.iterator();
            while (varIterator.hasNext()) {
                LiveRange var = varIterator.next();
                if (newSegment.isEmpty() || newSegment.doNotOverlap(var)) {
                    newSegment.add(var);
                    varIterator.remove();
                }
            }
            segments.add(newSegment);
        }

        // Ensure that parameters have good registers
        Set<Integer> usedRegisters = new HashSet<>();
        parametersIterator: for (int i = 0; i < parameters.size(); ++i) {
            for (Segment s : segments) {
                if (s.contains(parameters.get(i))) {
                    s.setRegister(i);
                    usedRegisters.add(i);
                    continue parametersIterator;
                }
            }
            alocationMap.put(parameters.get(i), i);
        }

        // Alocate registers to the rest of the segments
        int register = 0;
        for (Segment s : segments) {
            if (s.getRegisterNum() != null)
                continue;
            boolean continuee;
            do {
                continuee = false;
                if (usedRegisters.contains(register)) {
                    usedRegisters.remove(register++);
                    continuee = true;
                }
            } while (continuee);
            s.setRegister(register++);
        }

        segments.forEach(s -> {
            s.forEach(e -> alocationMap.put(e.getName(), s.getRegisterNum()));
            // System.out.println("---");
            // System.out.println(s.getRegisterNum());
            // s.getLiveRanges().forEach(el -> System.out.print(el.getName() +
            // ""));
            // System.out.println();

        });
        // System.out.println(alocationMap);
        return alocationMap;
    }

    private List<LiveRange> getLiveRanges(Collection<LivenessTableRow> livenessTable) {
        Map<String, LiveRange> liveRanges = new HashMap<>();

        livenessTable.forEach(row -> {
            row.getOut().forEach(out -> {
                if (!liveRanges.containsKey(out))
                    liveRanges.put(out, new LiveRange(out, row.getNum()));
            });
            row.getIn().forEach(in -> {
                if (!liveRanges.containsKey(in))
                    liveRanges.put(in, new LiveRange(in, row.getNum()));
                liveRanges.get(in).setEnd(row.getNum());
            });

        });

        return liveRanges.values().stream().collect(Collectors.toList());
    }
}
