package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;

/**
 * Interface definition for the Context Factories.
 */
public interface ContextFactory {
    public ContextFactory withClassLoader(final ClassLoader classLoader) throws ContextInitializationException;

    public ContextFactory withParentContext(final Context context) throws ContextInitializationException;

    public Context build() throws ContextInitializationException;
}
