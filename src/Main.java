import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import custom.Logger;
import ir.CodeBuilder;
import semantic.ModuleAnalyzer;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;

public class Main {
	public static final Logger LOGGER = Logger.INSTANCE;

	public static void main(String[] args) throws ParseException, FileNotFoundException {
		//String input = "./examples/programa3.yal";
		String input = args[0];
		SimpleNode module = new Yal2jvm(new java.io.FileInputStream(input)).Start();

		// Common.dump("", module);
		if (LOGGER.haveSyntacticErrors())
			return;
		CodeBuilder builder = new ModuleAnalyzer(module).analyze();
		if (LOGGER.haveSematicErrors())
			return;

		String fileName = new File(input).getName().replaceFirst("[.][^.]+$", "")+".j";
		try (PrintWriter out = new PrintWriter(fileName)) {
			out.println(builder.build());
		}
	}
}
