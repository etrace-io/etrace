package io.etrace.common.exception;

public class TooManyRedisException extends RuntimeException {
    public TooManyRedisException() {
        super();
    }

    public TooManyRedisException(String message) {
        super(message);
    }
}
