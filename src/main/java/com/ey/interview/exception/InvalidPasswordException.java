package com.ey.interview.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Password must contain at least one uppercase letter, lowercase letters, and two digits");
    }
}
