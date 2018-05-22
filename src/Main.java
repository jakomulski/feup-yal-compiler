import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import custom.Logger;
import ir.CodeBuilder;
import semantic.Common;
import semantic.ModuleAnalyzer;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;

public class Main {
    public final Logger LOGGER = Logger.getInstance();

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        // String input = "./new_examples_test/call-main.yal";
        // String input = args[0];

        Main main = new Main();
        main.printCode = false;

        for (String arg : args) {
            try {
                System.out.println("Reading: " + arg);
                new Main().run(arg);
                Logger.reset();
                System.out.println("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // main.run(input);
    }

    private boolean dump = false;
    private boolean printCode = false;

    public void run(String input) throws FileNotFoundException, ParseException {
        SimpleNode module = new Yal2jvm(new java.io.FileInputStream(input)).Start();

        if (dump)
            Common.dump("", module);

        if (LOGGER.haveSyntacticErrors())
            return;
        CodeBuilder builder = new ModuleAnalyzer(module).analyze();
        if (LOGGER.haveSematicErrors())
            return;

        String code = builder.build();

        generateFile(input, code);

        if (printCode)
            System.out.println(code);
    }

    private void generateFile(String input, String code) throws FileNotFoundException {
        File file = new File(input);
        String directory = file.getParent();
        String fileName = directory + "\\" + file.getName().replaceFirst("[.][^.]+$", "") + ".j";

        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(code);
            System.out.println("Generated: " + fileName);
        }

        String[] args = { "-d", directory, fileName };
        new jasmin.Main().run(args);
    }
}
