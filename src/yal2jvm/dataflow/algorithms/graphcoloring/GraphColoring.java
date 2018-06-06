package yal2jvm.dataflow.algorithms.graphcoloring;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.color.GreedyColoring;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import yal2jvm.common.Constants;
import yal2jvm.dataflow.LivenessTableRow;
import yal2jvm.dataflow.algorithms.RegisterAlocationAlgorithm;

public class GraphColoring implements RegisterAlocationAlgorithm {

    @Override
    public Map<String, Integer> alocate(List<String> parameters, List<String> localVariables,
            Collection<LivenessTableRow> livenessTable) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Collection<LiveRange> liveRanges = getLiveRanges(livenessTable);
        fillGraph(liveRanges, graph);
        createEdges(liveRanges, graph);

        VertexColoringAlgorithm<String> alg = new GreedyColoring<String, DefaultEdge>(graph);

        Coloring<String> coloring = alg.getColoring();
        int colorsNum = coloring.getNumberColors();
        if (Constants.NUMBER_OF_REGISTERS < colorsNum) {
            System.out.println("aborting execution..");
            System.out.println("Minimum number of JVM local variables is " + colorsNum);
            System.exit(1);
        }

        List<Segment> segments = coloring.getColorClasses().stream().map(c -> new Segment(c))
                .collect(Collectors.toList());

        return createAlocationMap(parameters, segments);
    }

    private Map<String, Integer> createAlocationMap(List<String> parameters, List<Segment> segments) {
        Map<String, Integer> alocationMap = new HashMap<>();

        // Ensure that parameters have good registers
        Set<Integer> usedRegisters = new HashSet<>();
        parametersIterator: for (int i = 0; i < parameters.size(); i++) {
            for (Segment s : segments) {
                if (s.contains(parameters.get(i))) {
                    if (s.getRegisterNum() == null) {
                        s.setRegister(i);
                        usedRegisters.add(i);
                    }
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

        // fill the alocation map
        segments.forEach(s -> {
            s.forEach(e -> alocationMap.put(e, s.getRegisterNum()));
        });

        return alocationMap;
    }

    private void createEdges(Collection<LiveRange> liveRanges, Graph<String, DefaultEdge> graph) {
        for (LiveRange first : liveRanges)
            for (LiveRange second : liveRanges) {
                if (first != second && first.doOverlap(second)) {
                    graph.addEdge(first.getName(), second.getName());
                    graph.addEdge(second.getName(), first.getName());
                }
            }

    }

    private void fillGraph(Collection<LiveRange> liveRanges, Graph<String, DefaultEdge> graph) {
        liveRanges.forEach(lr -> graph.addVertex(lr.getName()));
    }

    private List<LiveRange> getLiveRanges(Collection<LivenessTableRow> livenessTable) {
        Map<String, LiveRange> liveRanges = new HashMap<>();

        livenessTable.forEach(row -> {
            row.getOut().forEach(out -> {
                if (!liveRanges.containsKey(out))
                    liveRanges.put(out, new LiveRange(out).addOut(row.getNum()));
                else
                    liveRanges.get(out).addOut(row.getNum());

            });
            row.getIn().forEach(in -> {
                if (!liveRanges.containsKey(in))
                    liveRanges.put(in, new LiveRange(in).addIn(row.getNum()));
                else
                    liveRanges.get(in).addIn(row.getNum());
            });
        });

        return liveRanges.values().stream().collect(Collectors.toList());
    }

}
