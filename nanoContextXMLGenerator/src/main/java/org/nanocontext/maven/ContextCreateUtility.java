package org.nanocontext.maven;

import org.nanocontext.core.Context;
import org.nanocontext.xml.XMLContextFactory;
import org.nanocontext.core.exceptions.ContextInitializationException;

import javax.xml.bind.JAXBException;

public class ContextCreateUtility {

    public static Context create(final String contextResourceName)
            throws JAXBException, ContextInitializationException {
        return create(Thread.currentThread().getContextClassLoader(), contextResourceName);
    }

    public static Context create(final ClassLoader classLoader, final String contextResourceName)
            throws JAXBException, ContextInitializationException {
        Context ctx = new XMLContextFactory()
                .with(classLoader.getResourceAsStream(contextResourceName))
                .build();
        return ctx;
    }

}