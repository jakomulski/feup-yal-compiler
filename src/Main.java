import java.io.FileNotFoundException;

import custom.Logger;
import ir.GenerateCodeModule;
import scope.ModuleScope;
import semantic.Common;
import semantic.ModuleAnalyzer;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;


public class Main {
	public static final Logger LOGGER = Logger.INSTANCE;
	
	ModuleScope scopeTree = new ModuleScope();
	
	public static void main(String[] args) throws ParseException, FileNotFoundException {
		LOGGER.info("Reading from standard input...");
		//new Yal2jvm();
		String input = "./examples/programa3.yal";
		SimpleNode module = new Yal2jvm(new java.io.FileInputStream(input)).Start();
		
		
		Common.dump("", module);
		new ModuleAnalyzer(module).analyze();
		
		//new GenerateCodeModule().generateModule("", module);
		
		LOGGER.info("---");
		
	}
}
