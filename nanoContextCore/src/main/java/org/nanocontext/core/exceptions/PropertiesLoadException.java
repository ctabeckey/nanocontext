package org.nanocontext.core.exceptions;

import java.net.URL;

/**
 * Created by cbeckey on 4/7/17.
 */
public class PropertiesLoadException extends ContextInitializationException {
    private static String createMessage(final URL propertiesUrl) {
        return String.format("Unable to load properties from [%s]", propertiesUrl == null ? "<null>" : propertiesUrl.toExternalForm());
    }

    public PropertiesLoadException(final URL propertiesUrl) {
        super(createMessage(propertiesUrl));
    }
    public PropertiesLoadException(final URL propertiesUrl, Throwable t) {
        super(createMessage(propertiesUrl), t);
    }
}
