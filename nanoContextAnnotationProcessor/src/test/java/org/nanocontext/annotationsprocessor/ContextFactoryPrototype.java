package org.nanocontext.annotationsprocessor;

import org.nanocontext.core.*;
import org.nanocontext.core.exceptions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is simply a prototype to be used for authoring the context factory template.
 */
public class ContextFactoryPrototype implements org.nanocontext.core.ContextFactory {
    private ClassLoader classLoader = null;
    private org.nanocontext.core.Context parentContext = null;

    public ContextFactory withClassLoader(final ClassLoader classLoader) throws ContextInitializationException {
        this.classLoader = classLoader;
        return this;
    }

    public ContextFactory withParentContext(final org.nanocontext.core.Context parentContext) throws ContextInitializationException {
        this.parentContext = parentContext;
        return this;
    }

    public Context build() throws ContextInitializationException {
        Set<AbstractReferencableProperty> contextObjectsNameMap = buildBeansMap();


        Context ctx = parentContext != null ? new Context(parentContext) : new Context();
        ctx.setContextObjectsMap(contextObjectsNameMap);

        return ctx;
    }

    private Set<AbstractReferencableProperty> buildBeansMap() {
        Set<AbstractReferencableProperty> contextObjectsNameMap =
                new HashSet<>();



        return contextObjectsNameMap;
    }

}
