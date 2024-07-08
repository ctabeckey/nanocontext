package org.nanocontext.annotationsprocessor;

import org.nanocontext.core.*;
import org.nanocontext.core.exceptions.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is here to provide a prototype for a ContextFactory generation
 */
public class SampleContextFactory implements org.nanocontext.core.ContextFactory {
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
        Map<String, AbstractReferencableProperty> contextObjectsNameMap = new HashMap<String, AbstractReferencableProperty>();


        Context ctx = parentContext != null ? new Context(parentContext) : new Context();



        ctx.setContextObjectsMap(contextObjectsNameMap.entrySet().stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toSet())
        );

        return ctx;
    }

}
