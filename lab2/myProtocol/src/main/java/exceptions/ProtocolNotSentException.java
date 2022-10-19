package exceptions;

public class ProtocolNotSentException extends MyException{

    public ProtocolNotSentException() {
        super("protocol wasn't sent");
    }
}
