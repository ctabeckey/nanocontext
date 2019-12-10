package org.nanocontext.core.exceptions;

/**
 * Created by cbeckey on 1/20/17.
 */
public class InvalidFinalizeMethodException extends ContextInitializationException {
    private static String createMessage(final String beanClassName, String initializeMethodName) {
        return String.format("Unable to finalize instance of %s, method %s could not be found", beanClassName, initializeMethodName);
    }

    public <T> InvalidFinalizeMethodException(final String beanClassName, String finalizeMethodName) {
        super(createMessage(beanClassName, finalizeMethodName));
    }

    public <T> InvalidFinalizeMethodException(final String beanClassName, String finalizeMethodName, Throwable t) {
        super(createMessage(beanClassName, finalizeMethodName), t);
    }

}
