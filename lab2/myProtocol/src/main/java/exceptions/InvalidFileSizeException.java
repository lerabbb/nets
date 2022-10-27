package exceptions;

public class InvalidFileSizeException extends MyException{
    public InvalidFileSizeException(long actual, long expected){
        super("Size of file is " + actual + " GB. But max size is " + expected + " GB");
    }
}
