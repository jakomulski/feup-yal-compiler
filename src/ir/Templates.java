package ir;

public class Templates {

    public static String getFill() {
        // @formatter:off         
        return ".method public static &fill([II)V" + System.lineSeparator() 
             + ".limit locals 4" + System.lineSeparator() 
             + ".limit stack 3" + System.lineSeparator() 
             + "  iconst_0" + System.lineSeparator() 
             + "  istore_2" + System.lineSeparator()
             + "  aload_0" + System.lineSeparator()
             + "  arraylength" + System.lineSeparator()
             + "  istore_3" + System.lineSeparator()
             + " L5:" + System.lineSeparator()
             + "  iload_2" + System.lineSeparator()
             + "  iload_3" + System.lineSeparator()
             + "  if_icmpge L20" + System.lineSeparator()
             + "  aload_0" + System.lineSeparator()
             + "  iload_2" + System.lineSeparator()
             + "  iload_1" + System.lineSeparator()
             + "  iastore" + System.lineSeparator()
             + "  iinc 2 1" + System.lineSeparator()
             + " goto L5" + System.lineSeparator()
             + " L20:" + System.lineSeparator()
             + "  return" + System.lineSeparator()
             + ".end method";
        // @formatter:on
    }

    public static String getMain(String module) {
     // @formatter:off
        return ".method public static main([Ljava/lang/String;)V" + System.lineSeparator() + 
                "  .limit locals 1" + System.lineSeparator() + 
                "  .limit stack 0" + System.lineSeparator() + 
                "  invokestatic "+module+"/main()V" + System.lineSeparator() +
                "  return" + System.lineSeparator() +
                ".end method" + System.lineSeparator();
     // @formatter:on
    }
}
