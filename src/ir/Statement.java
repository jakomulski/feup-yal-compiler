package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import scope.Scope;
import scope.VariableDesc;
import scope.VariableDescFactory;
import scope.VariableType;
import yal2jvm.SimpleNode;

public class Statement {

	private SimpleNode node;
	Scope scope;
	private Statement ref = null;
	
	public Statement(SimpleNode node, Scope scope) { 
		this.node = node;
		this.scope = scope;
	}

	private final List<Supplier<String>> stringValue = new ArrayList<>();
	private VariableDesc type = VariableDescFactory.INSTANCE.createField(VariableType.NULL, false);
	
	public void append(Supplier<String> text){
		stringValue.add(()->text.get());
	}
	
	public void append(String text){
		stringValue.add(()->text);
	}
	
	public void bipush(String value){
		append("bipush "+value);
	}
	
	public void istore(String value){
		String name = scope.getVariable(value).getName();
		append("istore "+name);
	}
	
	public void iload(String value){
		String name = scope.getVariable(value).getName();
		append("iload "+name);
	}
	
	public void ineg() {
		append("ineg");
	}

	public void operator(String tokenValue) {
		switch(tokenValue){
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
	
	public List<Supplier<String>> getValue(){
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

	public void ldc(String value) {
		append("ldc "+value);
	}

	public void aload(String value) {
		String name = scope.getVariable(value).getName();
		append("aload "+name);
		
	}

	public void iastore() {
		append("iastore");
		
	}

	public void newarray() {
		append("newarray int");
	}

	public void astore(String value) {
		append("astore " + scope.getVariable(value).getName());
		
	}

	public void putstatic(String value) {
		VariableDesc varDesc= scope.getVariable(value);
		if (varDesc.is(VariableType.SCALAR))
			append("putstatic fields/"+value+" I");
		else if(varDesc.is(VariableType.ARRAY))
			append("putstatic fields/"+value+" [I");
		
	}

	public void arraylength() {
		append("arraylength");
		
	}

	public void iaload() {
		append("iaload");
	}

	public void invokestatic(String value) {
		append("invokestatic "+value);
	}

	public void setType(VariableDesc type) {
		this.type = type;	
	}
	
	public VariableType getType() {
		return this.type.getType();
	}
	
	public String typeAsJasmin(){
		if(type.getType().equals(VariableType.SCALAR) || type.getType().equals(VariableType.ANY))
			return "I";
		else if(type.getType().equals(VariableType.ARRAY))
			return "[I";
		else
			return "V";
	}

	public void label(String value) {
		this.name = value;
		append(value+":");
		
	}

	public void ificmp(String condition) {
		append(mapCondition(condition)+" "+getName()+"_END");
	}
	
	private String mapCondition(String condition){ //return opposite
		switch(condition){
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
}
