package custom;

import java.util.ArrayList;
import java.util.List;

import yal2jvm.ParseException;
import yal2jvm.SimpleNode;

public enum Logger {
    INSTANCE;
    private final static int MAX_ERRORS = 10;

    private final static boolean SHOW_INFO = false;

    private int counter;

    private Logger() {
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
        semanticErrors.add("ERROR " + node.getLineNumber() + ": " + info);
        System.out.println("ERROR " + node.getLineNumber() + ": " + info);
    }

    public void error(ParseException e) throws ParseException {
        System.out.println("ERROR " + e.currentToken.beginLine + ": " + "syntactic");
        syntacticErrors.add("ERROR " + e.currentToken.beginLine + ": " + "syntactic");
        counter--;

        if (counter == 0) {
            System.exit(1);
        }
    }

    public void semanticWarning(SimpleNode node, String info) {
        System.out.println("WARNING " + node.getLineNumber() + ": " + info);

    }
}
