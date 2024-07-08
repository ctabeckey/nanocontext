package org.nanocontext.xml.exceptions;

import org.nanocontext.core.exceptions.ContextInitializationException;

/**
 * Created by cbeckey on 2/11/16.
 */
public class InvalidArtifactSyntaxException extends ContextInitializationException {
    private static String createMessage(final org.nanocontext.xml.ResourceType artifactType) {
        return String.format("Exception when creating artifact (%s) %s", artifactType.getId(), artifactType.getClasspath().toString());
    }

    public InvalidArtifactSyntaxException(final org.nanocontext.xml.ResourceType artifactType, Throwable cause) {
        super(createMessage(artifactType), cause);
    }
}
