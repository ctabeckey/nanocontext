package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;

import java.util.List;

/**
 * Created by cbeckey on 2/8/16.
 */
public class PrototypeBeanInstanceFactory<T>
        extends AbstractBeanInstanceFactory<T> {

    /**
     *
     * @param context
     * @param id
     * @param artifactIdentifier
     * @param clazzName
     * @param ctorArgs
     * @throws ContextInitializationException
     */
    public PrototypeBeanInstanceFactory(
            final Context context,
            final String id,
            final String artifactIdentifier,
            final String clazzName,
            final String factoryId,
            final String factoryClassName,
            final String factoryMethodName,
            final boolean lazyLoad,
            final boolean active,
            final String activateMethod,
            final String initializeMethod,
            final String finalizeMethod,
            final List<AbstractProperty> ctorArgs)
            throws ContextInitializationException {
        super(context, id,
                artifactIdentifier, clazzName,
                factoryId, factoryClassName, factoryMethodName,
                lazyLoad,
                active, activateMethod,
                initializeMethod, finalizeMethod,
                ctorArgs);
    }

    /**
     *
     * @return
     * @throws ContextInitializationException
     */
    @Override
    public T getValue() throws ContextInitializationException {
        T instance = createBeanInstance();
        return instance;
    }

}
