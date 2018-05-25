package yal2jvm.scope;

public enum ScopeFactory {
    INSTANCE;

    private boolean moreScopes = true;

    private ModuleScope rootScope = new ModuleScope();

    public ModuleScope getModuleScope(String name) {
        rootScope.setModuleName(name);
        return rootScope;
    }

    public Scope createSimpleScope(Scope scope) {
        if (scope.getClass().equals(ModuleScope.class))
            return new SimpleScope(scope);

        if (moreScopes)
            return new SimpleScope(scope);
        return scope;
    }

    public BlockedSimpleScope createBlockedScope(Scope scope) {
        return new BlockedSimpleScope(scope);
    }

}
