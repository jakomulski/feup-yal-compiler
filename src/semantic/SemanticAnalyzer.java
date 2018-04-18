package semantic;

import yal2jvm.ASTModule;
import yal2jvm.SimpleNode;

public class SemanticAnalyzer {
	public SemanticAnalyzer(SimpleNode node){
		
		
	}
	
	
	public static void dump(String prefix, SimpleNode node) {
	    System.out.println(prefix + node.toString());
	    if (node.getChildren() != null) {
	      for (int i = 0; i < node.getChildren().length; ++i) {
	        SimpleNode n = (SimpleNode)node.getChildren()[i];
	        if (n != null) {
	          n.dump(prefix + " ");
	        }
	      }
	    }
	  }
}
