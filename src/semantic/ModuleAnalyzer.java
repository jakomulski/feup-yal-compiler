package semantic;

import static semantic.Common.checkSizeOf;
import static yal2jvm.Yal2jvmTreeConstants.JJTARRAYVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTDECLARE;
import static yal2jvm.Yal2jvmTreeConstants.JJTFUNCTION;
import static yal2jvm.Yal2jvmTreeConstants.JJTINTEGER;
import static yal2jvm.Yal2jvmTreeConstants.JJTSCALARVARIABLE;
import static yal2jvm.Yal2jvmTreeConstants.JJTSIZEOF;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import custom.Logger;
import ir.CodeBuilder;
import ir.IrBuilder;
import scope.FunctionDesc;
import scope.ModuleScope;
import scope.Scope;
import scope.ScopeFactory;
import scope.VariableDesc;
import scope.VariableDescFactory;
import scope.VariableType;

import yal2jvm.Node;
import yal2jvm.SimpleNode;

public class ModuleAnalyzer {
	private final Logger LOGGER = Logger.INSTANCE;
	private final SimpleNode module;
	private final ModuleScope rootScope;
	private final CodeBuilder codeBuilder;
	
	
	
	public ModuleAnalyzer(SimpleNode module) {
		this.module = module;
		this.rootScope = ScopeFactory.INSTANCE.getModuleScope();
		this.codeBuilder = new CodeBuilder(module, rootScope);
	}

	Queue<Runnable> toAnalyze = new LinkedList<>();

	public void analyze() {
		analyzeModule("", module);
		toAnalyze.forEach(r -> r.run());
		this.codeBuilder.build();
	}

	public void analyzeModule(String prefix, SimpleNode node) {
		LOGGER.semanticInfo(node, "class " + node.getTokenValue());
		for (Node n : node.getChildren())
			if (SimpleNode.class.cast(n).is(JJTFUNCTION))
				analyzeFunction(Common.cast(n));
			else //
				analyzeDeclaration(SimpleNode.class.cast(n));
	}

	private void analyzeDeclaration(SimpleNode node) {
		if (node.is(JJTDECLARE)) { // JJTDECLARE
			analyzeInitialization(node);
			return;
		}

		VariableDesc desc;
		String name;
		if (node.is(JJTARRAYVARIABLE)) {
			name = node.getTokenValue();
			desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, false);
			LOGGER.semanticInfo(node, "" + name + "[]");

		} else { // JJTSCALARVARIABLE
			name = node.getTokenValue();
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false);
			LOGGER.semanticInfo(node, "" + name);
		}
		rootScope.addVariable(name, desc);
	}

	private void analyzeInitialization(SimpleNode node) {
		VariableDesc desc;
		String name;

		name = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();
		node = (SimpleNode) node.jjtGetChild(1);
		if (node.is(JJTINTEGER)) {
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, true);
			String value = node.jjtGetValue() + node.getTokenValue();
			desc.setValue(Integer.parseInt(value));
			LOGGER.semanticInfo(node, "" + name + "=" + value);
			rootScope.addVariable(name, desc);
		} else { // JJTARRAY
			analyzeArrayInitialization((SimpleNode) node.jjtGetChild(0), name);
		}
	}

	private void analyzeArrayInitialization(SimpleNode node, String name) {
		VariableDesc desc;
		if (node.is(JJTINTEGER)) {
			int size = Integer.parseInt(node.getTokenValue());

			desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
			LOGGER.semanticInfo(node, "" + name + "=" + "[" + size + "]");
		} else if (node.is(JJTSIZEOF)) {
			if (!checkSizeOf(node, rootScope))
				return;
			else {
				VariableDesc varDesc = rootScope
						.getVariable(SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue());
				desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
				LOGGER.semanticInfo(node, name + "=" + "[ ]");
			}
		} else {
			String varName = node.getTokenValue();
			if (!rootScope.hasVariable(varName)) {
				LOGGER.semanticError(node, "undeclared");
				return;
			} else if (!rootScope.getVariable(varName).is(VariableType.SCALAR)) {
				LOGGER.semanticError(node, "incorrect type");
				return;
			} else {
				VariableDesc varDesc = rootScope.getVariable(varName);
				int size = varDesc.getValue();
				if (size < 1) {
					LOGGER.semanticError(node, "size of array has to be > 0");
					return;
				}
				desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
				LOGGER.semanticInfo(node, "" + name + "=" + "[" + size + "]");
			}
		}
		rootScope.addVariable(name, desc);
	}

	private void analyzeFunction(SimpleNode node) {

		Scope functionScope = ScopeFactory.INSTANCE.createSimpleScope(rootScope);
		String name = node.getTokenValue();
		
		FunctionDesc fnDesc = new FunctionDesc(name);

		Optional<VariableDesc> returnVar = analyzeFunctionReturn((SimpleNode) node.jjtGetChild(0), fnDesc,
				functionScope);
		analyzeFunctionParameters((SimpleNode) node.jjtGetChild(1), fnDesc, functionScope);

		rootScope.addFunction(name, fnDesc);
		SimpleNode statementsNode = (SimpleNode) node.jjtGetChild(2);
		toAnalyze.add(() -> {
			LOGGER.semanticInfo(node, "function: " + node.getTokenValue());

			IrBuilder irBuilder = new IrBuilder(fnDesc);
			codeBuilder.addIrBuilder(irBuilder);
			
			new StatementsAnalyzer(irBuilder).analyzeStatements(statementsNode, functionScope);

			returnVar.ifPresent(desc -> {
				if (!desc.isInitialized())
					LOGGER.semanticError(statementsNode, "return value is not initialized");
			});
			
			// TODO -> WARNING parameters
		});
	}

	private Optional<VariableDesc> analyzeFunctionReturn(SimpleNode returnNode, FunctionDesc fnDesc,
			Scope functionScope) {
		VariableDesc retVar = null;
		if (returnNode.jjtGetNumChildren() == 0) {
			fnDesc.setReturnType(VariableType.NULL);
		} else {
			returnNode = (SimpleNode) returnNode.jjtGetChild(0);
			if (returnNode.is(JJTARRAYVARIABLE)) {
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY, false);
				fnDesc.setReturnType(VariableType.ARRAY);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			} else if (returnNode.is(JJTSCALARVARIABLE)) {
				fnDesc.setReturnType(VariableType.SCALAR);
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			}
		}
		return Optional.ofNullable(retVar);
	}

	private void analyzeFunctionParameters(SimpleNode parametersNode, FunctionDesc fnDesc, Scope functionScope) {
		if (parametersNode.getChildren() != null)
			for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
				if (parameter.is(JJTARRAYVARIABLE)) {
					fnDesc.addArumentType(VariableType.ARRAY);
					functionScope.addVariable(parameter.getTokenValue(), VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY, true));
				} else if (parameter.is(JJTSCALARVARIABLE)) {
					fnDesc.addArumentType(VariableType.SCALAR);
					functionScope.addVariable(parameter.getTokenValue(), VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, true));
				}
			}
	}
}
