package yal2jvm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import jjtree.ParseException;
import jjtree.SimpleNode;
import jjtree.Yal2jvm;
import yal2jvm.common.Constants;
import yal2jvm.common.Logger;
import yal2jvm.common.NameGenerator;
import yal2jvm.ir.CodeBuilder;
import yal2jvm.semantic.Common;
import yal2jvm.semantic.ModuleAnalyzer;

public class Main {
    public final Logger LOGGER = Logger.getInstance();

    public static void main(String[] args) {
        run(args);
        // forTest();
    }

    private static void run(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                setOption(arg);
                continue;
            }
            try {
                System.out.println("Reading: " + arg);
                new Main().run(arg);
                System.out.println("");
            } catch (Exception e) {
                System.out.println("Not generated" + System.lineSeparator());
                e.printStackTrace();
            }
            Logger.reset();
            NameGenerator.reset();
        }
    }

    @SuppressWarnings("unused")
    private static void forTest() {
        String input = "./examples_test/test.yal";
        Constants.PRINT_CODE = true;
        Constants.GENERATE_J = true;
        Constants.OPTIMIZE = true;
        Constants.OPTIMIZED_REGISTER_ALOCATION = true;
        Main main = new Main();
        try {
            main.run(input);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("");
        }
    }

    private static void setOption(String option) {
        String op = option.substring(0, 2);
        switch (op) {
        case "-o":
            Constants.OPTIMIZE = true;
            System.out.println("Running with the optimization");
            break;
        case "-r":
            if (option.length() > 2) {
                String sNum = option.substring(3);
                try {
                    Constants.NUMBER_OF_REGISTERS = Integer.valueOf(sNum);
                } catch (Exception e) {
                    System.out.println("Unknown option: " + option);
                    return;
                }
                System.out.println("Register alocation with " + sNum + " registers");
                break;
            } else {
                Constants.OPTIMIZED_REGISTER_ALOCATION = true;
                System.out.println("Register alocation with the left edge algorithm");
                break;
            }
        default:
            System.out.println("Unknown option: " + option);
        }
    }

    public void run(String input) throws ParseException, FileNotFoundException {
        SimpleNode module;
        try {
            module = new Yal2jvm(new java.io.FileInputStream(input)).Start();
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + input);
            throw e;
        }

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
