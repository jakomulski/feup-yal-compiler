import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import custom.Logger;
import custom.YalMapper;
import yal2jvm.ASTModule;
import yal2jvm.ParseException;
import yal2jvm.SimpleNode;
import yal2jvm.Yal2jvm;
import yal2jvm.Yal2jvmTreeConstants;

public class Main {
	public static final Logger LOGGER = Logger.INSTANCE;
	
	public static void main(String[] args) throws ParseException, FileNotFoundException {
		LOGGER.info("Reading from standard input...");
		new Yal2jvm(new java.io.FileInputStream(args[0]));
		ASTModule module = Yal2jvm.Start();
		module.dump("");
		//module.init();
		//module.dump(" | ");
		LOGGER.info("---");
	}
}
