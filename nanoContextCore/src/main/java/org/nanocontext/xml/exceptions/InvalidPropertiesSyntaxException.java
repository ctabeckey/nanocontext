package org.nanocontext.xml.exceptions;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.ResourceType;

/**
 * Created by cbeckey on 2/11/16.
 */
public class InvalidPropertiesSyntaxException extends ContextInitializationException {
    private static String createMessage(final ResourceType propertiesType) {
        return String.format("Exception when creating properties (%s) %s",
                propertiesType == null ? "<null>" : propertiesType.getId(),
                propertiesType == null || propertiesType.getClasspath() == null ? "<null>" : propertiesType.getClasspath().toString()
        );
    }

    public InvalidPropertiesSyntaxException(final ResourceType propertiesType, Throwable cause) {
        super(createMessage(propertiesType), cause);
    }
}
