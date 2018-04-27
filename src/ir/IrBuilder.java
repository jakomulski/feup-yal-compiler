package ir;


import static yal2jvm.Yal2jvmTreeConstants.JJTARRAY;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTASSIGN;
import static yal2jvm.Yal2jvmTreeConstants.JJTCALL;
import static yal2jvm.Yal2jvmTreeConstants.JJTINTEGER;
import static yal2jvm.Yal2jvmTreeConstants.JJTMODULEACCESS;
import static yal2jvm.Yal2jvmTreeConstants.JJTNEGATION;
import static yal2jvm.Yal2jvmTreeConstants.JJTOPERATOR;
import static yal2jvm.Yal2jvmTreeConstants.JJTSIZEOF;
import static yal2jvm.Yal2jvmTreeConstants.JJTVARIABLE;

import java.util.ArrayList;
import java.util.List;

import scope.FunctionDesc;
import scope.Scope;
import scope.VariableDesc;
import scope.VariableType;
import yal2jvm.SimpleNode;

public class IrBuilder {

	private final FunctionDesc functionDesc;
	private List<Statement> statements = new ArrayList<>();

	public IrBuilder(FunctionDesc desc) {
		this.functionDesc = desc;
	}

	public void addStatement(SimpleNode node, Scope scope) {
		statements.add(new Statement(node, scope));
	}

	public void build() {
		 //.method public static 
		statements.forEach(statement -> {
			generateStatement(statement.getNode(), statement.scope);
		});
		
	}

	
	private void generateStatement(SimpleNode node, Scope scope){
		if (node.is(JJTCALL)) {
			generateCall(node, scope);
		} else if (node.is(JJTASSIGN)) {
			
			String varName = ((SimpleNode)node.jjtGetChild(0)).getTokenValue();
			VariableDesc varDesc = scope.getVariable(varName);
			
			if(!varDesc.is(VariableType.SCALAR))
				return;
			
			generateRHS((SimpleNode)node.jjtGetChild(1), scope);
			System.out.println("istore "+varName);
		}

	}
	
	private void generateRHS(SimpleNode assignment, Scope scope) {
	
		if (assignment.is(JJTARRAY)) {
		} else if (assignment.is(JJTVARIABLE)) {
			System.out.println("iload "+assignment.getTokenValue());
		} else if (assignment.is(JJTINTEGER)) {
			System.out.println("bipush "+assignment.getTokenValue());
		} else if (assignment.is(JJTOPERATOR)) {
			generateRHS((SimpleNode)assignment.jjtGetChild(0), scope);
			generateRHS((SimpleNode)assignment.jjtGetChild(1), scope);
			System.out.println(assignment.toString());
		} else if (assignment.is(JJTNEGATION)) {
			generateRHS((SimpleNode)assignment.jjtGetChild(0), scope);
			System.out.println("ineg");
		} else if (assignment.is(JJTCALL)) {
			generateCall(assignment, scope);
			//System.out.println(assignment.toString());
		} else if (assignment.is(JJTSIZEOF)) {
			System.out.println(assignment.toString());
		} else if (assignment.is(JJTARRAYACCESS)) {
			System.out.println(assignment.toString());
		} else if (assignment.is(JJTMODULEACCESS)) {
			System.out.println(assignment.toString());
		}
	}
	
	private void generateCall(SimpleNode node, Scope scope){
		String name = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();
		System.out.println("invokestatic "+scope.getFunction(name).asJasmin());	
	}
	
}
