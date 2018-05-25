package yal2jvm.dataflow.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yal2jvm.dataflow.LivenessTableRow;

public class LeftEdgeAlgorithm implements RegisterAlocationAlgorithm {

    @Override
    public Map<String, Integer> alocate(List<String> parameters, List<String> localVariables,
            Collection<LivenessTableRow> livenessTable) {
        Map<String, Integer> alocationMap = new HashMap<>();

        List<LiveRange> variables = getLiveRanges(livenessTable);
        // Sort by the start time
        variables.sort((f, s) -> f.getStart().compareTo(s.getStart()));

        variables.forEach(r -> System.out.println(r.getName() + ": " + r.getStart() + " " + r.getEnd()));

        List<Segment> segments = new ArrayList<>();
        while (!variables.isEmpty()) {
            Segment newSegment = new Segment();
            Iterator<LiveRange> varIterator = variables.iterator();
            while (varIterator.hasNext()) {
                LiveRange var = varIterator.next();
                if (newSegment.getLiveRanges().isEmpty() || doNotOverlap(var, newSegment.getLiveRanges())) {
                    newSegment.getLiveRanges().add(var);
                    varIterator.remove();
                }
            }
            segments.add(newSegment);
        }

        // parameters.forEach(action);

        // int i = 0;
        // for (; i < parameters.size(); ++i) {
        // String parameter = parameters.get(i);
        // Iterator<Set<LiveRange>> segmentIterator = segments.iterator();
        // while (segmentIterator.hasNext()) {
        // Set<LiveRange> set = segmentIterator.next();
        // if (set.contains(parameter)) {
        // for (LiveRange lRElement : set)
        // alocationMap.put(lRElement.getName(), i);
        // parameter = null;
        // segmentIterator.remove();
        // break;
        // }
        // }
        // if (parameter != null) {
        // alocationMap.put(parameter, i);
        // }
        // }
        // --i;
        segments.forEach(s -> {
            s.getLiveRanges().forEach(el -> System.out.print(el.getName() + " "));
            System.out.println("-");
        });
        for (int i = 0; i < segments.size(); ++i) {
            int num = i;
            segments.get(i).getLiveRanges().forEach(el -> alocationMap.put(el.getName(), num));
        }

        System.out.println(alocationMap);
        return alocationMap;
    }

    private boolean doNotOverlap(LiveRange first, Set<LiveRange> currentSegment) {
        for (LiveRange second : currentSegment)
            if (first.getStart() > second.getStart() && first.getStart() < second.getEnd()
                    || first.getEnd() > second.getStart() && first.getEnd() < second.getEnd())
                return false;
        return true;
    }

    private List<LiveRange> getLiveRanges(Collection<LivenessTableRow> livenessTable) {
        Map<String, LiveRange> liveRanges = new HashMap<>();

        livenessTable.forEach(row -> {
            row.getOut().forEach(out -> {
                if (!liveRanges.containsKey(out))
                    liveRanges.put(out, new LiveRange(out, row.getNum()));
            });
            row.getIn().forEach(in -> liveRanges.get(in).setEnd(row.getNum()));
        });

        return liveRanges.values().stream().collect(Collectors.toList());
    }
}

class LiveRange {
    private final Integer start;
    private Integer end;
    private final String name;

    LiveRange(String name, Integer start) {
        this.start = start;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}