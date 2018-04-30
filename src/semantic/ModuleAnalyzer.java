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
		this.rootScope = ScopeFactory.INSTANCE.getModuleScope(module.getTokenValue());
		this.codeBuilder = new CodeBuilder(module, rootScope);
	}

	Queue<Runnable> toAnalyze = new LinkedList<>();

	public CodeBuilder analyze() {
		analyzeModule(module);
		toAnalyze.forEach(r -> r.run());
		return this.codeBuilder;
	}

	public void analyzeModule(SimpleNode node) {
		for (Node n : node.getChildren())
			if (Common.cast(n).is(JJTFUNCTION))
				analyzeFunction(Common.cast(n));
			else
				analyzeDeclaration(Common.cast(n));
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
			codeBuilder.addArrayDeclaration(name);

		} else { // JJTSCALARVARIABLE
			name = node.getTokenValue();
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, false);
			codeBuilder.addScalarDeclaration(name);
		}
		rootScope.addVariable(name, desc);
	}

	private void analyzeInitialization(SimpleNode node) {
		VariableDesc desc;
		String name;

		name = node.jjtGetChild(0).getTokenValue();
		node =   node.jjtGetChild(1);
		if (node.is(JJTINTEGER)) {
			desc = VariableDescFactory.INSTANCE.createField(VariableType.SCALAR, true);
			String value = node.getTokenValue();
			desc.setValue(value);
			codeBuilder.addScalarInitialization(name, value);
			rootScope.addVariable(name, desc);
		} else { // JJTARRAY
			analyzeArrayInitialization(  node.jjtGetChild(0), name);
		}
	}

	private void analyzeArrayInitialization(SimpleNode node, String name) {
		VariableDesc desc;
		if (node.is(JJTINTEGER)) {
			String size = node.getTokenValue();

			desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
			desc.setValue(size);
			codeBuilder.addArrayInitialization(name, size);
		} else if (node.is(JJTSIZEOF)) {
			if (!checkSizeOf(node, rootScope))
				return;
			else {
				VariableDesc varDesc = rootScope
						.getVariable(node.jjtGetChild(0).getTokenValue());
				desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
				codeBuilder.addArrayInitialization(name, varDesc.getValue());
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
				String size = varDesc.getValue();
				if (size.startsWith("-")) {
					LOGGER.semanticError(node, "size of array has to be > 0");
					return;
				}
				desc = VariableDescFactory.INSTANCE.createField(VariableType.ARRAY, true);
				desc.setValue(size);
				codeBuilder.addArrayInitialization(name, size);
			}
		}
		rootScope.addVariable(name, desc);
	}

	private void analyzeFunction(SimpleNode node) {

		Scope functionScope = ScopeFactory.INSTANCE.createSimpleScope(rootScope);
		String name = node.getTokenValue();
		
		FunctionDesc fnDesc = new FunctionDesc(name);
		IrBuilder irBuilder = new IrBuilder(fnDesc);
		codeBuilder.addIrBuilder(irBuilder);
		VariableDesc returnVar = analyzeFunctionReturn(node.jjtGetChild(0), fnDesc,
				functionScope);
		analyzeFunctionParameters(  node.jjtGetChild(1), fnDesc, functionScope, irBuilder);

		rootScope.addFunction(name, fnDesc);
		SimpleNode statementsNode =   node.jjtGetChild(2);
		toAnalyze.add(() -> {
			LOGGER.semanticInfo(node, "function: " + node.getTokenValue());

			new StatementsAnalyzer(irBuilder).analyzeStatements(statementsNode, functionScope);
						
			irBuilder.addReturnValue(returnVar);
			if (!returnVar.isInitialized())
				LOGGER.semanticError(statementsNode, "return value is not initialized");
			
			// TODO -> WARNINGS
		});
	}

	private VariableDesc analyzeFunctionReturn(SimpleNode returnNode, FunctionDesc fnDesc,
			Scope functionScope) {
		VariableDesc retVar = null;
		if (returnNode.jjtGetNumChildren() == 0) {
			retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.NULL, true);
			fnDesc.setReturnType(VariableType.NULL);
		} else {
			returnNode = returnNode.jjtGetChild(0);
			if (returnNode.is(JJTARRAYVARIABLE)) {
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY, false);
				fnDesc.setReturnType(VariableType.ARRAY);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			} else if (returnNode.is(JJTSCALARVARIABLE)) {
				retVar = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, false);
				fnDesc.setReturnType(VariableType.SCALAR);
				functionScope.addVariable(returnNode.getTokenValue(), retVar);
			}
		}
		return retVar;
	}

	private void analyzeFunctionParameters(SimpleNode parametersNode, FunctionDesc fnDesc, Scope functionScope, IrBuilder irBuilder) {
		Integer counter = 0;
		if (parametersNode.getChildren() != null)
			for (Node n : parametersNode.getChildren()) {
				SimpleNode parameter = (SimpleNode) n;
				if (parameter.is(JJTARRAYVARIABLE)) {
					fnDesc.addArumentType(VariableType.ARRAY);
					VariableDesc paramDesc = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY, true);
					functionScope.addVariable(parameter.getTokenValue(), paramDesc);
					irBuilder.addParameter(paramDesc);
				} else if (parameter.is(JJTSCALARVARIABLE)) {
					VariableDesc paramDesc = VariableDescFactory.INSTANCE.createLocalVariable(VariableType.SCALAR, true);
					fnDesc.addArumentType(VariableType.SCALAR);
					functionScope.addVariable(parameter.getTokenValue(), paramDesc);
					irBuilder.addParameter(paramDesc);
				}
				counter++;
			}
	}
}
