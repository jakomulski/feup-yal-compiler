package custom;

public class NameGenerator {
    public static NameGenerator INSTANCE = new NameGenerator();

    public static void reset() {
        INSTANCE = new NameGenerator();
    }

    public NameGenerator() {

    }

    int counter = 0;

    public String getName() {
        return "L" + counter++;
    }

    public String getLoopName() {
        return "L" + counter++;
    }

    public String getIfName() {
        return "L" + counter++;
    }
}
