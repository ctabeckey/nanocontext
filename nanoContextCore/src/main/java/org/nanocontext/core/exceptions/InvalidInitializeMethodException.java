package org.nanocontext.core.exceptions;

/**
 * Created by cbeckey on 1/20/17.
 */
public class InvalidInitializeMethodException extends ContextInitializationException {
    private static String createMessage(final String beanClassName, String initializeMethodName) {
        return String.format("Unable to initialize instance of %s, initialize method %s could not be found", beanClassName, initializeMethodName);
    }

    public <T> InvalidInitializeMethodException(final String beanClassName, String initializeMethodName) {
        super(createMessage(beanClassName, initializeMethodName));
    }

    public <T> InvalidInitializeMethodException(final String beanClassName, String initializeMethodName, Throwable t) {
        super(createMessage(beanClassName, initializeMethodName), t);
    }

}
