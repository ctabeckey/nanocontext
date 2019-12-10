package org.nanocontext.core.exceptions;

/**
 * Created by cbeckey on 2/4/16.
 */
public class BeanClassNotFoundException extends ContextInitializationException {
    private static String createMessage(String clazzName) {
        return String.format("Unable to find referenced bean class %s", clazzName == null ? "<null>" : clazzName);
    }

    public BeanClassNotFoundException(String clazzName) {
        super(createMessage(clazzName));
    }

    public BeanClassNotFoundException(String clazzName, Throwable cause) {
        super(createMessage(clazzName), cause);
    }
}
