package scope;

class FunctionScope extends Scope {
	public FunctionScope(ModuleScope parent) {
		super(parent);
	}

	@Override
	public FunctionDesc getFunction(String name) {
		return parent.getFunction(name);
	}
}