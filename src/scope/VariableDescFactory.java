package scope;

public enum VariableDescFactory {
	INSTANCE;
	
	public VariableDesc createLocalVariable(VariableType type, boolean initialized){
		return new VariableDescImpl(type, initialized);
	}
	
	public VariableDesc createField(VariableType type, boolean initialized){
		VariableDescImpl varDesc = new VariableDescImpl(type, initialized);
		varDesc.isField = true;
		return varDesc;
	}
	
	public VariableDesc createCopy(VariableDesc variableDesc){
		return new VariableDescImpl(variableDesc);
	}

	class VariableDescImpl implements VariableDesc{
		private String name = "";
		private boolean initialized;
		private VariableType type;
		private boolean isField = false;
		
		private int value = 0;
		
		
		public void setName(String name) {
			this.name = name;
		}

		public VariableDescImpl(VariableDesc variableDesc){
			name = variableDesc.getName();
			type = variableDesc.getType();
			initialized = variableDesc.isInitialized();
			value = variableDesc.getValue();
			isField = variableDesc.isField();
		}
		
		public VariableDescImpl(VariableType type, boolean initialized){
			this.type = type;
			this.initialized = initialized;
		}
			
		public VariableDescImpl initialize(){	
			this.initialized = true;
			return this;
		}
		
		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public boolean isField() {
			return isField;
		}
		
		public boolean isInitialized() {
			return initialized;
		}


		public boolean is(VariableType type){
			return this.type.equals(type);
		}
		
		public VariableType getType(){
			return type;
		}
		
		public VariableDescImpl setType(VariableType type) {
			this.type = type;
			return this;
		}

		
		@Override
		public String toString() {
			return "VariableDesc [ initialized=" + initialized + ", type=" + type + "]";
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
