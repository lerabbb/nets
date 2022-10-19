package exceptions;

public class InvalidArgumentsException extends MyException{
    public InvalidArgumentsException(int real, int expected) {
        super("Number of arguments is " + real + ". But expected " + expected);
    }
}

