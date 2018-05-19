package semantic;

import custom.Logger;
import scope.Scope;
import scope.VariableDesc;
import scope.VariableType;
import yal2jvm.Node;
import yal2jvm.SimpleNode;

public class Common {
    private static final Logger LOGGER = Logger.INSTANCE;

    public static boolean checkSizeOf(SimpleNode node, Scope scope) {
        String varName = SimpleNode.class.cast(node.jjtGetChild(0)).getTokenValue();

        if (!scope.hasVariable(varName)) {
            LOGGER.semanticError(node, "undeclared");
            return false;
        } else if (!scope.getVariable(varName).isInitialized()) {
            LOGGER.semanticError(node, "uninitialized");
            return false;
        } else if (!scope.getVariable(varName).is(VariableType.ARRAY)) {
            LOGGER.semanticError(node, "incorrect type");
            return false;
        }
        return true;
    }

    public static SimpleNode cast(Node node) {
        return SimpleNode.class.cast(node);
    }

    public static boolean isInt(String value) {
        return value.matches("\\d+");
    }

    public static boolean isUninitialized(VariableDesc desc, SimpleNode node) {
        if (!desc.isInitialized()) {
            if (desc.isField()) {
                LOGGER.semanticWarning(node, "might not be initialized");
                return false;
            }
            LOGGER.semanticError(node, "not initialized");
            return true;
        }
        return false;
    }

    public static boolean checkUndeclaredAndUninitialized(Scope scope, SimpleNode node) {
        String name = node.getTokenValue();
        if (!scope.hasVariable(name)) {
            LOGGER.semanticError(node, "undeclared");
            return false;
        } else if (isUninitialized(scope.getVariable(name), node)) {
            return false;
        }
        return true;
    }

    public static void dump(String prefix, SimpleNode node) {
        System.out.println(prefix + node.toString() + " " + node.getTokenValue());
        if (node.getChildren() != null) {
            for (int i = 0; i < node.getChildren().length; ++i) {
                SimpleNode n = (SimpleNode) node.getChildren()[i];
                if (n != null) {
                    dump(prefix + " ", n);
                }
            }
        }
    }
}
