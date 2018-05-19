package scope;

import java.util.ArrayList;
import java.util.List;

public class FunctionDesc {

    private VariableType returnType;
    private List<VariableType> arumentsTypes = new ArrayList<>();
    private final String name;

    public FunctionDesc(String name) {
        this.name = name;
    }

    public VariableType getReturnType() {
        return returnType;
    }

    public int getParamsNum() {
        return arumentsTypes.size();
    }

    public void setReturnType(VariableType returnType) {
        this.returnType = returnType;
    }

    public List<VariableType> getArumentsTypes() {
        return arumentsTypes;
    }

    public void addArumentType(VariableType arumentType) {
        this.arumentsTypes.add(arumentType);
    }

    @Override
    public String toString() {
        return "FunctionDesc [returnType=" + returnType + ", arumentsTypes=" + arumentsTypes + "]";
    }

    public String getName() {
        return name;
    }

    public String asJasminVoid() {
        StringBuilder builder = new StringBuilder(name + "(");
        this.arumentsTypes.forEach(t -> builder.append(typeAsJasmin(t)));
        builder.append(")V");

        return builder.toString();
    }

    public String asJasmin() {
        StringBuilder builder = new StringBuilder(name + "(");
        this.arumentsTypes.forEach(t -> builder.append(typeAsJasmin(t)));
        builder.append(")");

        if (returnType.equals(VariableType.NULL))
            builder.append("V");
        else
            builder.append(typeAsJasmin(returnType));
        return builder.toString();
    }

    public String typeAsJasmin(VariableType type) {
        return type.equals(VariableType.SCALAR) ? "I" : "[I";
    }
}
