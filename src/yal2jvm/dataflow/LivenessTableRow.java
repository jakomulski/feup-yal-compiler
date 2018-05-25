package yal2jvm.dataflow;

import java.util.Set;

public interface LivenessTableRow {
    int getNum();

    Set<String> getDef();

    Set<String> getUse();

    Set<String> getIn();

    Set<String> getOut();

}
