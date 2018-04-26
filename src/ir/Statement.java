package ir;

import scope.Scope;
import yal2jvm.SimpleNode;

public class Statement {

	SimpleNode node;
	Scope scope;
	
	public Statement(SimpleNode node, Scope scope) {
		this.node = node;
		this.scope = scope;
	}

	public SimpleNode getNode() {
		return node;
	}

	public Scope getScope() {
		return scope;
	}

}
