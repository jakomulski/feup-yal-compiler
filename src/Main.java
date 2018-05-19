import java.io.FileNotFoundException;

import custom.Logger;
import ir.CodeBuilder;
import semantic.ModuleAnalyzer;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;

public class Main {
    public static final Logger LOGGER = Logger.INSTANCE;

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        String input = "./examples/programa1.yal";
        // String input = args[0];
        SimpleNode module = new Yal2jvm(new java.io.FileInputStream(input)).Start();

        // Common.dump("", module);
        if (LOGGER.haveSyntacticErrors())
            return;
        CodeBuilder builder = new ModuleAnalyzer(module).analyze();
        if (LOGGER.haveSematicErrors())
            return;

        // String fileName = new File(input).getName().replaceFirst("[.][^.]+$",
        // "")+".j";
        // try (PrintWriter out = new PrintWriter(fileName)) {
        // out.println(builder.build());
        // }

        System.out.println(builder.build());

        // VariableDesc desc =
        // VariableDescFactory.INSTANCE.createLocalVariable(VariableType.ARRAY,
        // false);
        // Statement st = new Statement();
        // AddOperation istore = st.add(new IStore(desc));
        // AddOperation addOperation = istore.add(new IAdd());
        // addOperation.add(new INeg()).add(new BiPush("4"));
        // addOperation.add(new BiPush("1"));
        // AddOperation addOperation2 = addOperation.add(new IAdd());
        // addOperation2.add(new INeg()).add(new BiPush("10"));
        // addOperation2.add(new BiPush("20"));
        // System.out.println(st);

    }
}
