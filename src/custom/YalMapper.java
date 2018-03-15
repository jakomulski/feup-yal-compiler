package custom;

import java.util.List;

public class YalMapper {

	public static String yalClass(String name) {
		return ".class public " + name + "\n.super java/lang/Object";
	}

	public static String yalMethodStart(String name, List<String> parameters, String returnValue) {
		if("main".equals(name))
			return ".method public static main([Ljava/lang/String;)V";
		
		String yalLine = ".method public static " + name + "("
				+ new String(new char[parameters.size()]).replace("\0", "I") + ")";
		if(returnValue != null)
			return yalLine+"I";
		return yalLine;
	}
}
