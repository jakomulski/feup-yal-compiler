package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import operations.IPush;
import operations.LowIrNode;
import optimization.Optimizer;
import scope.Scope;

public class CodeBuilder {

    private final Scope rootScope;

    private List<String> fieldsCode = new ArrayList<>();
    private List<String> staticCode = new ArrayList<>();

    public String build() {
        StringBuilder code = new StringBuilder();

        Consumer<String> append = s -> {
            code.append(s);
            code.append(System.lineSeparator());
        };

        append.accept(".class public " + rootScope.getModuleName());
        append.accept(".super java/lang/Object");
        append.accept("");

        fieldsCode.forEach(line -> append.accept(line));

        append.accept("");

        if (!staticCode.isEmpty()) {
            append.accept(".method static public <clinit>()V");
            append.accept(".limit stack 2");
            append.accept(".limit locals 1");
            staticCode.forEach(line -> append.accept("  " + line));
            append.accept("return");
            append.accept(".end method");
        }

        functions.forEach(f -> {
            // append.accept("");
            if (f.getDescription().isMain())
                append.accept(Templates.getMain(rootScope.getModuleName()));
            f.build(new Optimizer()).forEach(line -> append.accept(line));
        });

        append.accept("");
        append.accept(Templates.getFill());

        return code.toString();
    }

    private void appendCode(String text) {
        fieldsCode.add(text);
    }

    private void appendStaticCode(String text) {
        staticCode.add(text);
    }

    private final List<IrBuilder> functions = new ArrayList<>();

    public void addArrayDeclaration(String name) {
        appendCode(".field static " + name + " [I");
    }

    public void addScalarDeclaration(String name) {
        appendCode(".field static " + name + " I");
    }

    public void addArrayFill(String name, String value) {
        appendStaticCode("getstatic " + rootScope.getModuleName() + "/" + name + " [I");

        LowIrNode biPushNode = new LowIrNode(new IPush(value));
        biPushNode.getOperation().optimize();
        appendStaticCode(biPushNode.getOperation().toString());
        appendStaticCode("invokestatic " + rootScope.getModuleName() + "/&fill([II)V");
    }

    public CodeBuilder(Scope scope) {
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
        // appendStaticCode(";" + name + " = new int[" + value + "]");
        appendStaticCode("bipush " + value);
        appendStaticCode("newarray int");
        appendStaticCode("putstatic " + this.rootScope.getModuleName() + "/" + name + " [I");
    };
}
