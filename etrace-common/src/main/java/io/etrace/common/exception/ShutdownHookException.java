package io.etrace.common.exception;

public class ShutdownHookException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ShutdownHookException() {
        super();
    }

    public ShutdownHookException(String message) {
        super(message);
    }

    public ShutdownHookException(Throwable cause) {
        super(cause);
    }

    public ShutdownHookException(String message, Throwable cause) {
        super(message, cause);
    }
}
