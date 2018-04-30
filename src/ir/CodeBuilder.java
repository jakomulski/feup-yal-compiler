package ir;

import java.util.ArrayList;
import java.util.List;

import scope.Scope;
import yal2jvm.SimpleNode;

public class CodeBuilder {

	private final SimpleNode moduleNode;
	private final Scope rootScope;

	private List<String> fieldsCode = new ArrayList<>();
	private List<String> staticCode = new ArrayList<>();
	
	
	public void build() {
		System.out.println(".class public " + rootScope.getModuleName());
		System.out.println(".super java/lang/Object");
		System.out.println("");
		
		
		fieldsCode.forEach(line -> System.out.println(line));

		System.out.println("");

		if (!staticCode.isEmpty()) {
			System.out.println(".method static public <clinit>()V");
			staticCode.forEach(line -> System.out.println("  " + line));
			System.out.println("  return");
			System.out.println(".end method");
		}
		functions.forEach(f -> {
			System.out.println("");
			f.build().forEach(line -> System.out.println(line));
		});

	}

	private void appendCode(String text) {
		fieldsCode.add(text);
	}

	private void appendStaticCode(String text) {
		staticCode.add(text);
	}

	private final List<IrBuilder> functions = new ArrayList<>();

	public void addArrayDeclaration(String name) {
		appendCode(".field static " + name + " I");
	}

	public void addScalarDeclaration(String name) {
		appendCode(".field static " + name + " [I");
	}

	public CodeBuilder(SimpleNode node, Scope scope) {
		this.moduleNode = node;
		this.rootScope = scope;
	}

	public void addIrBuilder(IrBuilder irBuilder) {
		functions.add(irBuilder);
	}

	public void addScalarInitialization(String name, String value) {
		appendCode(".field static " + name + " I = " + value);

	};

	public void addArrayInitialization(String name, String value) {
		appendCode(".field static " + name + " [I");
		appendStaticCode(";" + name + " = new int[" + value + "]");
		appendStaticCode("bipush " + value);
		appendStaticCode("newarray int");
		appendStaticCode("putstatic fields/" + name + " [I");
	};
}
