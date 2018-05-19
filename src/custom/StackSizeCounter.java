package custom;

public class StackSizeCounter {

    int counter = 0;
    int maxSize = 0;

    public StackSizeCounter() {

    }

    public int getStackSize() {
        return maxSize;
    }

    public void push() {
        counter++;
        if (maxSize < counter)
            maxSize = counter;
    }

    public void pop() {
        counter--;
    }
}
