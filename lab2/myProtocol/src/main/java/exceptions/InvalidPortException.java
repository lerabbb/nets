package exceptions;

public class InvalidPortException extends MyException{

    public InvalidPortException(int real, int expected) {
        super("Entered posrt is " + real + ". But max port is " + expected);
    }
}
