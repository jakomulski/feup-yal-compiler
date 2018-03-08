public class Logger {
	private int counter;
	public Logger(int maxErrors){
		counter = maxErrors;
	}
	
	public void error(ParseException e) throws ParseException{
		System.out.println(e);
		counter--;
		if(counter == 0)
			throw e;
	}
}
