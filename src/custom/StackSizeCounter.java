package custom;

import java.util.concurrent.atomic.AtomicInteger;

public class StackSizeCounter {
	
	int counter = 0;
	int maxSize = 0;
	
	
	public StackSizeCounter(){
		
	}
	
	public int getStackSize(){
		return maxSize;
	}
	
	public void increment(){
		counter++;
		if(maxSize < counter)
			maxSize = counter;
	}
	
	public void decrement(){
		counter--;
	}
}
