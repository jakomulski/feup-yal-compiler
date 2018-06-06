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
        if (args.length == 0)
            help();

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
        Constants.PRINT_CODE = false;
        Constants.GENERATE_J = false;
        Constants.OPTIMIZE = false;
        Constants.OPTIMIZED_REGISTER_ALOCATION = true;
        Constants.REGISTER_ALOCATION_BY_GRAPH_COLORING = true;
        Constants.NUMBER_OF_REGISTERS = 10;
        Main main = new Main();
        try {
            main.run(input);
            System.out.println("------");
            // Runtime rt = Runtime.getRuntime();
            // Process pr = rt.exec("cmd.exe /C cd examples_test && java test");
            //
            // BufferedReader in = new BufferedReader(new
            // InputStreamReader(pr.getErrorStream()));
            // String line = null;
            // while ((line = in.readLine()) != null) {
            // System.out.println(line);
            // }
            //
            // in = new BufferedReader(new
            // InputStreamReader(pr.getInputStream()));
            // line = null;
            // while ((line = in.readLine()) != null) {
            // System.out.println(line);
            // }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("");
        }
    }

    private static void setOption(String option) {
        String op = option.substring(0, 2);
        switch (op) {
        case "-h":
            help();
            break;
        case "-o":
            Constants.OPTIMIZE = true;
            System.out.println("Running with the optimization");
            break;
        case "-d":
            Constants.DUMP = true;
            break;
        case "-p":
            Constants.PRINT_CODE = true;
            break;
        case "-r":
            if (option.length() > 2) {
                String sNum = option.substring(3);
                try {
                    Constants.OPTIMIZED_REGISTER_ALOCATION = true;
                    Constants.NUMBER_OF_REGISTERS = Integer.valueOf(sNum);
                    Constants.REGISTER_ALOCATION_BY_GRAPH_COLORING = true;
                } catch (Exception e) {
                    System.out.println("Unknown option: " + option);
                    System.exit(0);
                }
                System.out.println("local variables alocation with " + sNum + " registers");
                break;
            }
        default:
            System.out.println("Unknown option: " + option);
            System.out.println("Use -h for help");
            System.exit(0);
        }
    }

    private static void help() {
        System.out.println("Usage: java -jar yal2jvm [-options] [input...] ");
        System.out.println("");
        System.out.println("yal2jvm compiler generates two files: .j and .class");
        System.out.println("");
        System.out.println("Options: ");
        System.out.println(" -o \t\t optimization (constant propagation, constant folding, dead code elimination)");
        System.out.println(
                " -r=<num> \t local variables alocation with <num> registers and the graph coloring algorithm");
        System.out.println("");
        System.out.println(" -p \t\t print jasmin code ");
        System.out.println(" -d \t\t dump AST");
        System.out.println(" -h \t\t help ( this ) ");
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
