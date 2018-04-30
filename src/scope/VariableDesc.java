package scope;

public interface VariableDesc {
	VariableDesc initialize();
	String getValue();
	void setValue(String value);
	boolean isInitialized();
	boolean isField();
	boolean is(VariableType type);
	VariableType getType();
	VariableDesc setType(VariableType type);
	String getName();
	void setName(String name);
	
}
