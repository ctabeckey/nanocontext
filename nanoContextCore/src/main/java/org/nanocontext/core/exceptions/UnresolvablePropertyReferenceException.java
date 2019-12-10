package org.nanocontext.core.exceptions;

/**
 * An exception that is thrown when a property contains a reference to another property that
 * does not exist.
 */
public class UnresolvablePropertyReferenceException extends ContextInitializationException {
    private static String createMessage(final String propertyReference) {
        return String.format("Unable to reolve property reference [%s].", propertyReference);
    }

    public UnresolvablePropertyReferenceException(final String propertyReference) {
        super(createMessage(propertyReference));
    }

    public UnresolvablePropertyReferenceException(final String propertyReference, final Throwable t) {
        super(createMessage(propertyReference), t);
    }
}

