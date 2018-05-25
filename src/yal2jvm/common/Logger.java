package yal2jvm.common;

import java.util.ArrayList;
import java.util.List;

import jjtree.ParseException;
import jjtree.SimpleNode;

public interface Logger {

    static void reset() {
        LoggerImp.class.cast(LoggerImp.INSTANCE).semanticErrors.clear();
        LoggerImp.class.cast(LoggerImp.INSTANCE).syntacticErrors.clear();
    }

    static Logger getInstance() {
        return LoggerImp.INSTANCE;
    }

    void semanticInfo(SimpleNode node, String info);

    boolean haveSematicErrors();

    boolean haveSyntacticErrors();

    void semanticError(SimpleNode node, String info);

    void error(ParseException e) throws ParseException;

    void semanticWarning(SimpleNode node, String info);

    class LoggerImp implements Logger {
        static Logger INSTANCE = new LoggerImp();

        private final static int MAX_ERRORS = 10;

        private final static boolean SHOW_INFO = false;

        private int counter;

        private LoggerImp() {
            counter = MAX_ERRORS;
        }

        public List<String> semanticErrors = new ArrayList<>();
        public List<String> syntacticErrors = new ArrayList<>();

        public void info(Object info) {
            System.out.println(info);
        }

        public void yal(Object info) {
            // System.out.println(info);
        }

        public void semanticInfo(SimpleNode node, String info) {
            // String name = node.getClass().getName();
            if (SHOW_INFO)
                System.out.println(+node.getLineNumber() + ": INFO " + " " + info);
        }

        public boolean haveSematicErrors() {
            return semanticErrors.size() > 0;
        }

        public boolean haveSyntacticErrors() {
            return syntacticErrors.size() > 0;
        }

        public void semanticError(SimpleNode node, String info) {
            semanticErrors.add("LINE " + node.getLineNumber() + ": " + "SEMANTIC ERROR " + info);
            System.out.println("LINE " + node.getLineNumber() + ": " + "SEMANTIC ERROR " + info);
        }

        public void error(ParseException e) throws ParseException {
            System.out.println("LINE " + e.currentToken.beginLine + ": " + "SYNTATIC ERROR " + e.getLocalizedMessage());
            syntacticErrors
                    .add("LINE " + e.currentToken.beginLine + ": " + "SYNTATIC ERROR " + e.getLocalizedMessage());
            counter--;

            if (counter == 0) {
                System.exit(1);
            }
        }

        public void semanticWarning(SimpleNode node, String info) {
            System.out.println("WARNING " + node.getLineNumber() + ": " + info);

        }
    }
}
