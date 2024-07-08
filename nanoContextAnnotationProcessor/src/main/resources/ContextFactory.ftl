package ${packageName};

import org.nanocontext.core.*;
import org.nanocontext.core.exceptions.*;

import java.util.*;
import java.util.stream.*;

public class ${contextFactoryName} implements org.nanocontext.core.ContextFactory {
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
