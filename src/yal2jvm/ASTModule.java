/* Generated By:JJTree: Do not edit this line. ASTModule.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package yal2jvm;

import java.util.HashMap;

import custom.ASTWithName;
import custom.Scope;

public class ASTModule extends SimpleNode {
	private Scope scope = new Scope();

	private void putFunction(ASTFunction function) throws ParseException {
		if (scope.functions().containsKey((function).name))
			throw new ParseException();
		scope.functions().put(function.name, function);
	}

	private void putVariable(String variable) throws ParseException {
		if (scope.variables().containsKey(variable))
			throw new ParseException();
		scope.variables().put(variable, null);
	}

	private void assignVariable(ASTAssign node) throws ParseException {
		String variableName = ((ASTWithName) node.jjtGetChild(0)).getName(); // is
																				// ASTArrayVariable
																				// or
																				// ASTScalarVariable
		putVariable(variableName); // checking if it is initialized

		Node child = node.jjtGetChild(1);
		if (ASTArray.class.equals(child.getClass())){
			((ASTArray)child).decompose(scope);
		}
		decomposeArrayNode(node.jjtGetChild(1));
		scope.variables().put(variableName, (SimpleNode) node.jjtGetChild(1));
	}

	// TODO: clean code
	private void decomposeArrayNode(Node node) throws ParseException {
		if (ASTArray.class.equals(node.getClass())) { // is array
			Node child = node.jjtGetChild(0);
			if (ASTVariable.class.equals(child.getClass())) { // variable
				String var = ((ASTVariable) child).name;
				SimpleNode integerNode = scope.variables().get(var);
				if (integerNode == null || !integerNode.getClass().equals(ASTInteger.class))
					throw new ParseException();
				else {
					((SimpleNode) node).children = null;
					node.jjtAddChild(integerNode, integerNode.id);
				}
			} else if (ASTSizeof.class.equals(child.getClass())) {
				String var = ((ASTVariable) child.jjtGetChild(0)).name;
				SimpleNode arrayNode = scope.variables().get(var);
				if (arrayNode == null || !arrayNode.getClass().equals(ASTArray.class))
					throw new ParseException();
				else {
					((SimpleNode) node).children = null;
					SimpleNode integerNode = (SimpleNode) arrayNode.jjtGetChild(0);
					node.jjtAddChild(integerNode, integerNode.id);
				}
			}
		}
	}

	private HashMap<Class<? extends SimpleNode>, CheckedConsumer<SimpleNode>> mapper = new HashMap<>();
	{
		mapper.put(ASTFunction.class, n -> putFunction((ASTFunction) n));
		mapper.put(ASTArrayVariable.class, n -> putVariable(((ASTArrayVariable) n).name));
		mapper.put(ASTScalarVariable.class, n -> putVariable(((ASTScalarVariable) n).name));
		mapper.put(ASTAssign.class, n -> assignVariable((ASTAssign) n));
		// TODO - attributes
	}

	private void map(Node node) throws ParseException {
		if (mapper.containsKey(node.getClass())) {
			mapper.get(node.getClass()).apply((SimpleNode) node);
		}
	}

	public void init() throws ParseException {
		for (Node n : children)
			map(n);
		System.out.println(scope.functions().keySet());
		System.out.println(scope.variables());
	}

	public ASTModule(int id) {
		super(id);
	}

	public ASTModule(Yal2jvm p, int id) {
		super(p, id);
	}
}
/*
 * JavaCC - OriginalChecksum=100ef1b6e39cf9e27d8b68e14528c8ee (do not edit this
 * line)
 */

@FunctionalInterface
interface CheckedConsumer<T extends Node> {
	void apply(T t) throws ParseException;
}