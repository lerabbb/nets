package nsu.lerabbb.lab5.proxy.exceptions;

public class ProxyServerException extends Throwable {
    public ProxyServerException() {
        super();
    }

    public ProxyServerException(String message) {
        super(message);
    }

    public ProxyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyServerException(Throwable cause) {
        super(cause);
    }

    protected ProxyServerException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
