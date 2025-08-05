package com.beartrail.user.exception;

import java.io.Serial;

public class UserNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
