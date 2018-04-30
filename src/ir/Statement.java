package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import custom.StackSizeCounter;
import scope.FunctionDesc;
import scope.Scope;
import scope.VariableDesc;
import scope.VariableDescFactory;
import scope.VariableType;
import yal2jvm.SimpleNode;

public class Statement {

	private StackSizeCounter stackSizeCounter = null;
	private SimpleNode node;
	Scope scope;
	private Statement ref = null;

	public Statement getRef() {
		return ref;
	}

	private void incrementStackSize() {
		if(stackSizeCounter != null)
			stackSizeCounter.increment();
	}

	private void decrementStackSize() {
		if(stackSizeCounter != null)
			stackSizeCounter.decrement();
	}
	

	public void setStackSizeCounter(StackSizeCounter stackSizeCounter) {
		this.stackSizeCounter = stackSizeCounter;
	}

	public Statement setRef(Statement ref) {
		this.ref = ref;
		return this;
	}

	public Statement() {
		this.node = new SimpleNode(-1);
		this.name = NameGenerator.INSTANCE.getName();
	}

	public Statement(SimpleNode node, Scope scope) {
		this.node = node;
		this.scope = scope;
	}

	private final List<Supplier<String>> stringValue = new ArrayList<>();
	private VariableDesc type = VariableDescFactory.INSTANCE.createField(VariableType.NULL, false);

	public List<Supplier<String>> getValue() {
		return stringValue;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimpleNode getNode() {
		return node;
	}

	public Scope getScope() {
		return scope;
	}
	
	public void append(Supplier<String> text) {
		stringValue.add(() -> text.get());
	}

	public void append(String text) {
		stringValue.add(() -> text);
	}

	public void bipush(String value) {
		append("bipush " + value);
		incrementStackSize();
	}

	public void istore(VariableDesc value) {
		decrementStackSize();
		append("istore " + value.getName());
	}

	public void iload(VariableDesc value) {
		incrementStackSize();
		append("iload " + value.getName());
	}

	public void ineg() {
		incrementStackSize();
		decrementStackSize();
		append("ineg");
	}

	public void operator(String tokenValue) {
		decrementStackSize();
		decrementStackSize();
		incrementStackSize();
		
		switch (tokenValue) {
		case "+":
			append("iadd");
			return;
		case "-":
			append("isub");
			return;
		case "*":
			append("imul");
			return;
		case "/":
			append("idiv");
			return;
		case ">>":
			append("ishr");
			return;
		case "<<":
			append("ishl");
			return;
		case ">>>":
			append("iushr");
			return;
		case "&":
			append("iand");
			return;
		case "|":
			append("ior");
			return;
		case "^":
			append("ixor");
			return;
		}
	}


	public void ldc(String value) {
		incrementStackSize();
		append("ldc " + value);
	}

	public void aload(VariableDesc value) {
		incrementStackSize();
		append("aload " + value.getName());

	}

	public void iastore() {
		decrementStackSize();
		decrementStackSize();
		decrementStackSize();
		append("iastore");

	}

	public void newarray() {
		decrementStackSize();
		incrementStackSize();
		append("newarray int");
	}

	public void astore(VariableDesc value) {
		decrementStackSize();
		append("astore " + value.getName());

	}

	public void putstatic(VariableDesc value) {
		decrementStackSize();
		if (value.is(VariableType.SCALAR))
			append("putstatic fields/" + value.getName() + " I");
		else if (value.is(VariableType.ARRAY))
			append("putstatic fields/" + value.getName() + " [I");

	}

	public void arraylength() {
		decrementStackSize();
		incrementStackSize();
		append("arraylength");

	}

	public void iaload() {
		decrementStackSize();
		decrementStackSize();
		incrementStackSize();
		append("iaload");
	}

	public void invokestatic(String name) {
		FunctionDesc fnDesc = scope.getFunction(name);
		fnDesc.getArumentsTypes().forEach(e->decrementStackSize());
		append(() -> "invokestatic " + scope.getModuleName() + "/" + fnDesc.asJasmin());
	}

	public void invokestatic(String module, String name, List<String> args) {
		args.forEach(e->decrementStackSize());
		append(() -> "invokestatic " + module + "/" + name + "(" + String.join("", args) + ")" + typeAsJasmin());
	}

	

	

	public void label(String value) {
		append(value + ":");

	}

	public void ificmp(String condition) {
		decrementStackSize();
		decrementStackSize();
		append(mapCondition(condition) + " " + ref.getName());
	}

	private String mapCondition(String condition) { // return opposite
		switch (condition) {
		case "==":
			return "if_icmpne";
		case "!=":
			return "if_icmpeq";
		case ">":
			return "if_icmple";
		case "<":
			return "if_icmpge";
		case ">=":
			return "if_icmpge";
		case "<=":
			return "if_icmpgt";
		}
		return "";
	}

	public void ireturn() {
		append("ireturn");

	}

	public void returnn() {
		decrementStackSize();
		append("return");
	}
	
	public void setType(VariableDesc type) {
		this.type = type;
	}
	
	public VariableType getType() {
		return this.type.getType();
	}

	public String typeAsJasmin() {
		if (type.getType().equals(VariableType.SCALAR) || type.getType().equals(VariableType.ANY))
			return "I";
		else if (type.getType().equals(VariableType.ARRAY))
			return "[I";
		else
			return "V";
	}
}
