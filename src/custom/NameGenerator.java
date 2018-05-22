package custom;

public enum NameGenerator {
    INSTANCE;
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
