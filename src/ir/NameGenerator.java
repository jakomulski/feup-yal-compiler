package ir;

public enum NameGenerator {
	INSTANCE;
	int counter = 0;
	
	public String getLoopName(){
		return ""+counter++;
	}

	public String getIfName() {
		return ""+counter++;
	}
}
