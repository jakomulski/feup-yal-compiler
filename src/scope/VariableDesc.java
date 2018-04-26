package scope;

public interface VariableDesc {
	VariableDesc initialize();
	int getValue();
	void setValue(int value);
	boolean isInitialized();
	boolean isField();
	boolean is(VariableType type);
	VariableType getType();
	VariableDesc setType(VariableType type);
	String getName();
	void setName(String name);
}
