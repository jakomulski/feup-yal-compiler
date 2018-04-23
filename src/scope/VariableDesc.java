package scope;



public class VariableDesc {
	public static final int ANY_SIZE = -1;
	
	private int size = 0;
	private boolean initialized;
	private VariableType type;
	private int value = 0;
	
	public VariableDesc(VariableType type, boolean initialized){
		this.type = type;
		this.initialized = initialized;
	}
		
	public VariableDesc initialize(){
		this.initialized = true;
		return this;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public int getSize() {
		return size;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public boolean is(VariableType type){
		return this.type.equals(type);
	}
	
	public VariableType getType(){
		return type;
	}
	
	public VariableDesc setType(VariableType type) {
		this.type = type;
		return this;
	}

	public VariableDesc withSize(int size){
		this.size = size;
		return this;
	}

	@Override
	public String toString() {
		return "VariableDesc [size=" + size + ", initialized=" + initialized + ", type=" + type + "]";
	}
	
	
}
