package scope;

public enum ScopeFactory {
	INSTANCE;
	
	private boolean moreScopes = true;
	
	private ModuleScope rootScope = new ModuleScope();
	
	public ModuleScope getModuleScope(){
		return rootScope;
	}
	public Scope createSimpleScope(Scope scope){
		if(scope.getClass() == ModuleScope.class)
			return new SimpleScope(scope);
		
		if(moreScopes)
			return new SimpleScope(scope);
		return scope;
	}
}
