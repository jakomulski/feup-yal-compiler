import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	private final Map<String, Symbol> table = new HashMap<>();
	
	
	public Symbol globalScope(String var){
		return null;
	}
	
	public Symbol localScope(String function, String var){
		return null;
	}
}
