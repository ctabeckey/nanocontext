package org.nanocontext.xml.exceptions;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.ResourceType;

/**
 * Created by cbeckey on 2/11/16.
 */
public class InvalidArtifactSyntaxException extends ContextInitializationException {
    private static String createMessage(final ResourceType artifactType) {
        return String.format("Exception when creating artifact (%s) %s", artifactType.getId(), artifactType.getClasspath().toString());
    }

    public InvalidArtifactSyntaxException(final ResourceType artifactType, Throwable cause) {
        super(createMessage(artifactType), cause);
    }
}
