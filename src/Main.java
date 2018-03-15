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

	public static void main(String[] args) throws ParseException {
		LOGGER.info("Reading from standard input...");
		new Yal2jvm(System.in);
		// try {
		ASTModule module = Yal2jvm.Start();
		module.dump("");
		//module.init();
		LOGGER.info("---");
		LOGGER.info("---");
	}
}
