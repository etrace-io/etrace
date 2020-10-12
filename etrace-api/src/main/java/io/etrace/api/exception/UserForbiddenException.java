package io.etrace.api.exception;

import java.io.IOException;

public class UserForbiddenException extends IOException {

    public UserForbiddenException(String message) {
        super(message);
    }
}
