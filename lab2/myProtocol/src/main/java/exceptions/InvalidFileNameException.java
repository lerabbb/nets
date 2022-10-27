package exceptions;

public class InvalidFileNameException extends MyException{
    public InvalidFileNameException(int actual, int expected){
        super("Length of file name is " + actual + ". But max length is " + expected);
    }
}
