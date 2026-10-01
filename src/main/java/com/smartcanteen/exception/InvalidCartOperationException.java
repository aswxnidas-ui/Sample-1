package com.smartcanteen.exception;

/**
 * Custom checked exception thrown when an invalid operation is performed on a Cart
 * (e.g., adding non-positive quantity, removing an item not in cart, or checking out an empty cart).
 */
public class InvalidCartOperationException extends Exception {

    public InvalidCartOperationException(String message) {
        super(message);
    }

    public InvalidCartOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
