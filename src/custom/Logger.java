package custom;

import yal2jvm.ParseException;

public enum Logger {
	INSTANCE;
	private final static int MAX_ERRORS = 10;

	private int counter;

	private Logger() {
		counter = MAX_ERRORS;
	}

	public void info(Object info) {
		System.out.println(info);
	}

	public void yal(Object info) {
		// System.out.println(info);
	}

	public void error(ParseException e) throws ParseException {
		System.out.println(e.getMessage());
		counter--;

		if (counter == 0) {
			System.exit(1);
		}
	}
}
