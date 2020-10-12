package io.etrace.api.exception;

import java.io.IOException;

public class UnauthorizedException extends IOException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
