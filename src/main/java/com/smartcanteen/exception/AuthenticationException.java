package com.smartcanteen.exception;

/**
 * Custom checked exception thrown when an authentication or authorization
 * error occurs (e.g., invalid email, incorrect password, inactive student).
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
