package yal2jvm.dataflow;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import yal2jvm.ir.Statement;
import yal2jvm.scope.VariableDesc;

class Visitor {
    Visitor(Statement value) {
        this.value = value;
    }

    int num;
    Statement value;
    Visitor next;
    Visitor ref;

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

    @Override
    public String toString() {
        return this.num + ": " + printVarDescList(this.in) + " " + printVarDescList(this.out);
    }

    public String printVarDescList(Set<VariableDesc> list) {
        return list.stream().map(e -> e.getName()).collect(Collectors.toSet()).toString();
    }

    public LivenessTableRow convert() {
        Visitor self = this;
        return new LivenessTableRow() {
            private String stringValue = self.toString();
            private Set<String> use = self.use.stream().map(s -> s.getName()).collect(Collectors.toSet());
            private Set<String> def = self.def.stream().map(s -> s.getName()).collect(Collectors.toSet());
            private Set<String> in = self.in.stream().map(s -> s.getName()).collect(Collectors.toSet());
            private Set<String> out = self.out.stream().map(s -> s.getName()).collect(Collectors.toSet());
            private int num = self.num;

            @Override
            public Set<String> getUse() {
                return this.use;
            }

            @Override
            public Set<String> getOut() {
                return this.out;
            }

            @Override
            public Set<String> getIn() {
                return this.in;
            }

            @Override
            public Set<String> getDef() {
                return this.def;
            }

            @Override
            public String toString() {
                return stringValue;
            }

            @Override
            public int getNum() {
                return this.num;
            }
        };
    }
}
