package org.nanocontext.core.exceptions;

/**
 * The base class for exceptions that occur during context initialization.
 */
public class ContextInitializationException extends Exception {
    public ContextInitializationException() {
    }

    public ContextInitializationException(final String message) {
        super(message);
    }

    public ContextInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ContextInitializationException(final Throwable cause) {
        super(cause);
    }
}
