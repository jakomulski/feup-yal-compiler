package scope;



public class VariableDesc {
	
	private int size = 0;
	private boolean initialized;
	private final VariableType type;
	public VariableDesc(VariableType type, boolean initialized){
		this.type = type;
		this.initialized = initialized;
	}
	
	public VariableType getType(){
		return type;
	}
	
	public void setSize(int size){
		this.size = size;
		
	}

	@Override
	public String toString() {
		return "VariableDesc [size=" + size + ", initialized=" + initialized + ", type=" + type + "]";
	}
	
	
}
