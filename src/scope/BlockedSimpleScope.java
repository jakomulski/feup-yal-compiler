package scope;

import java.util.HashMap;
import java.util.Map;

public class BlockedSimpleScope extends SimpleScope {

	public BlockedSimpleScope(Scope parent) {
		super(parent);
	}

	protected Map<String, VariableDesc> blocked = new HashMap<>();

	@Override
	public VariableDesc getVariable(String name) {
		if (this.variables.containsKey(name))
			return this.variables.get(name);
		if (this.blocked.containsKey(name))
			return this.blocked.get(name);
		if (parent != null) {
			VariableDesc var = parent.getVariable(name);
			if (!var.isInitialized()) {
				var = VariableDescFactory.INSTANCE.createCopy(var);
				blocked.put(name, var);
			}
			return var;
		}
		return null;
	}
}
