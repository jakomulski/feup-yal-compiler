package scope;

public class SimpleScope extends Scope {

	public SimpleScope(Scope parent) {
		super(parent);
	}

	@Override
	public FunctionDesc getFunction(String name) {
		return parent.getFunction(name);
	}

}
