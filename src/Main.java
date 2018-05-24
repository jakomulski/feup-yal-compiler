import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import custom.Constants;
import custom.Logger;
import custom.NameGenerator;
import ir.CodeBuilder;
import semantic.Common;
import semantic.ModuleAnalyzer;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;

public class Main {
    public final Logger LOGGER = Logger.getInstance();

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        String input = "./examples_test/test.yal";
        // String input = args[0];

        Main main = new Main();
        Constants.GENERATE_LOCALS = false;
        Constants.PRINT_CODE = true;
        Constants.GENERATE_J = false;
        Constants.DUMP = false;
        Constants.OPTIMIZE = false;
        for (String arg : args) {
            try {
                System.out.println("Reading: " + arg);
                new Main().run(arg);
                System.out.println("");
            } catch (Exception e) {
                System.out.println("Not generated" + System.lineSeparator());
            }
            Logger.reset();
            NameGenerator.reset();
        }

        try {
            main.run(input);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("");
        }
    }

    public void run(String input) throws FileNotFoundException, ParseException {
        SimpleNode module = new Yal2jvm(new java.io.FileInputStream(input)).Start();

        if (Constants.DUMP)
            Common.dump("", module);

        if (LOGGER.haveSyntacticErrors())
            return;
        CodeBuilder builder = new ModuleAnalyzer(module).analyze();
        if (LOGGER.haveSematicErrors())
            return;

        String code = builder.build();

        if (Constants.GENERATE_J)
            generateFile(input, code);

        if (Constants.PRINT_CODE)
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

        if (Constants.GENERATE_CLASS) {
            String[] args = { "-d", directory, fileName };
            new jasmin.Main().run(args);
        }
    }
}
