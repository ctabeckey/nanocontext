package org.nanocontext.core;

import org.nanocontext.core.exceptions.*;
import org.nanocontext.utility.Utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The base class for bean Factory References, that is any type created from
 * a 'bean' element in the context definition XML.
 * NOTE: creating a BeanFactoryReference also creates references to
 * any beans referenced as constructor arguments in the context definition XML.
 *
 * @see AbstractProperty
 *
 * Known Derivations
 * @see PrototypeBeanInstanceFactory
 * @see SingletonBeanInstanceFactory
 */
public abstract class AbstractBeanInstanceFactory<T>
        extends AbstractReferencableProperty<T> {
    private final String artifactIdentifier;
    private final String clazzName;
    private final String factoryId;
    private final String factoryClassName;
    private final String factoryMethodName;
    private final boolean lazyLoad;
    private final boolean active;
    private final String activateMethod;
    private final String initializeMethod;
    private final String finalizeMethod;

    private final List<AbstractProperty> ctorParameters;

    /** */
    private final ReentrantLock beanClassLock = new ReentrantLock();

    /** */
    private Class<T> beanClass;
    private Set<AbstractProperty> constructorParameterProperties;

    /**
     * @param context
     * @param id
     * @param artifactIdentifier
     * @param clazzName
     * @param factoryId
     * @param factoryClassName
     * @param factoryMethodName
     * @param lazyLoad
     * @param active
     * @param activateMethod
     * @param ctorParameters
     * @throws ContextInitializationException
     */
    protected AbstractBeanInstanceFactory(
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
            final List<AbstractProperty> ctorParameters
    ) throws ContextInitializationException {
        super(context, id);
        this.artifactIdentifier = artifactIdentifier;
        this.clazzName = Utility.isPropertyReference(clazzName) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(clazzName)) :
                clazzName;

        this.factoryId = Utility.isPropertyReference(factoryId) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(factoryId)) :
                factoryId;

        this.factoryClassName = Utility.isPropertyReference(factoryClassName) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(factoryClassName)) :
                factoryClassName;

        this.factoryMethodName = Utility.isPropertyReference(factoryMethodName) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(factoryMethodName)) :
                factoryMethodName;

        // a bean cannot be lazy loaded if it is active
        // active implies immediate load and start
        this.lazyLoad = lazyLoad && !active;
        this.active = active;

        this.activateMethod = Utility.isPropertyReference(activateMethod) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(activateMethod)) :
                activateMethod;

        this.initializeMethod = Utility.isPropertyReference(initializeMethod) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(initializeMethod)) :
                initializeMethod;
        this.finalizeMethod = Utility.isPropertyReference(finalizeMethod) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(finalizeMethod)) :
                finalizeMethod;;

        this.ctorParameters = ctorParameters;
    }

    public String getClazzName() {
        return this.clazzName;
    }

    /**
     *
     * @return
     */
    public List<AbstractProperty> getConstructorParameterProperties() {
        return ctorParameters;
    }

    @Override
    public void initialize() throws ContextInitializationException {
        getValue();
    }

    /**
     *
     * @return
     */
    private URLClassLoader getArtifactClassLoader() {
        if (artifactIdentifier != null) {
            ArtifactHolder holder = getContext().getArtifactHolder(artifactIdentifier);
            return holder.getClassLoader();
        }
        return null;
    }

    /**
     *
     * @return
     * @throws ContextInitializationException
     */
    public T createBeanInstance() throws ContextInitializationException {
        T result = null;

        // if the factory method name is given without a factory ID then
        // try to create the bean treating the method as a static method
        if (factoryId == null && factoryMethodName != null) {
            result = createBeanInstanceUsingStaticFactory();

        // if the factory ID and the factory method name are given then try
        // to create the bean treating the method as an instance method on the bean
        // identified as the factory
        } else if (factoryId != null && factoryMethodName != null) {
            result = createBeanInstanceUsingFactory();

        // if the factory class or the factory ID exist with no method
        // then throw an exception
        } else if (factoryClassName != null || factoryId != null) {
            throw new InvalidBeanFactoryConfigurationException(this.getIdentifier());

        // If none of factory class, identifier or method are given, create the bean using a constructor
        } else {
            result = createBeanInstanceUsingConstructor();
        }

        // If an initialize method name is given then call that method on the newly created
        // instance. Note that the initialize method is called before the thread is started
        // if the bean is active.
        if (this.initializeMethod != null && this.initializeMethod.length() > 0) {
            try {
                Method initializeMethod = getValueType().getMethod(this.initializeMethod);
                // catch all throwable from this invocation and report it
                try {
                    initializeMethod.invoke(result);
                } catch (Throwable t) {
                    throw new InvalidInitializeMethodException(this.getIdentifier(), this.initializeMethod, t);
                }
            } catch (NoSuchMethodException e) {
                throw new InvalidInitializeMethodException(this.getIdentifier(), this.initializeMethod);
            }
        }

        // if the bean is marked as active and the bean has been created then
        // if there is a start method specified, call that to activate the instance
        // if it implements Runnable then start it.
        // else throw a ContextInitializationException
        // Note that Thread is not supported so that this class can manage ThreadGroup
        // membership
        Thread activeThread = null;
        if (active && result != null) {
            if (activateMethod != null) {
                String threadName = String.format("ActiveClassifier-%s-%s", this.getIdentifier(), this.activateMethod);
                activeThread = new Thread(getContext().getContextThreadGroup(),
                        new StartableInstance(this.getIdentifier(), this.activateMethod, result),
                        threadName
                );
                activeThread.setUncaughtExceptionHandler(
                        new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                if (e.getCause() != null && e.getCause() instanceof ContextInitializationException) {
                                    ContextInitializationException ciX = (ContextInitializationException)e.getCause();
                                    deferredContextInitializationException(ciX);
                                }
                            }
                        }
                );

            } else if (Runnable.class.isAssignableFrom(result.getClass())) {
                Runnable activeBean = (Runnable)result;
                String threadName = String.format("ActiveClassifier-%s", this.getIdentifier());
                activeThread = new Thread(getContext().getContextThreadGroup(), activeBean, threadName);
                getContext().registerActiveBean(activeBean);

            } else {
                throw new InvalidActiveClassAttributionException(this.getIdentifier(), this.getClazzName());
            }

            if (activeThread != null) {
                activeThread.start();
            }
        }

        return result;
    }

    /**
     *
     * @param ciX
     */
    private void deferredContextInitializationException(final ContextInitializationException ciX) {
        System.err.println(ciX.getMessage());
    }

    /**
     * Create the bean using a static factory method of either a factory bean if given or
     * the class of the bean itself if not
     *
     * @return
     * @throws ContextInitializationException
     */
    private T createBeanInstanceUsingStaticFactory() throws ContextInitializationException {
        try {
            final String effectiveFactoryClassName = this.factoryClassName == null ? this.clazzName : this.factoryClassName;
            Class<?> factoryClass = Class.forName(effectiveFactoryClassName);

            Method factoryMethod = ContextUtility.selectStaticFactoryMethod(factoryClass, this.factoryMethodName, ctorParameters, getValueType());
            if (factoryMethod != null) {
                Object[] parameters = createArguments(ctorParameters, factoryMethod.getParameterTypes());

                try {
                    //detectCircularReferences(this);
                    T instance = (T) factoryMethod.invoke(null, parameters);
                    //clearReferences();

                    return instance;
                } catch (ClassCastException | IllegalAccessException  | InvocationTargetException | IllegalArgumentException x) {
                    throw new FailedToInstantiateBeanException(clazzName, x);
                }
            } else {
                throw new InvalidStaticFactoryException(this.beanClass, this.factoryClassName, this.factoryMethodName);
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidStaticFactoryException(this.beanClass, this.factoryClassName);
        }

    }

    /**
     * Create the bean using an existing bean as a factory class
     * @return
     * @throws ContextInitializationException
     */
    private T createBeanInstanceUsingFactory() throws ContextInitializationException {
        AbstractProperty<?> factoryBean = this.getContext().getBeanReference(this.factoryId);

        if (factoryBean != null) {
            Method factoryMethod = ContextUtility.selectFactoryMethod(factoryBean.getValue(), this.factoryMethodName, ctorParameters, getValueType());
            if (factoryMethod != null) {
                try {
                    Object[] parameters = createArguments(ctorParameters, factoryMethod.getParameterTypes());

                    Object bean = factoryMethod.invoke(factoryBean.getValue(), parameters);
                    return (T)bean;
                } catch (ClassCastException | IllegalAccessException | InvocationTargetException e) {
                    throw new InvalidFactoryIdentifierException(this.clazzName, this.factoryId, this.factoryMethodName, e);
                }
            } else {
                throw new InvalidFactoryIdentifierException(this.clazzName, this.factoryId, this.factoryMethodName);
            }
        } else {
            throw new InvalidFactoryIdentifierException(this.clazzName, this.factoryId);
        }
    }

    /**
     * Create the bean using a constructor
     * @return
     * @throws ContextInitializationException
     */
    private T createBeanInstanceUsingConstructor() throws ContextInitializationException {
        //getContextFactory().build();
        Constructor<?> ctor = ContextUtility.selectConstructor(getValueType(), ctorParameters);
        if (ctor != null) {
            Object[] parameters = createArguments(ctorParameters, ctor.getParameterTypes());

            try {
                //detectCircularReferences(this);
                T instance = (T) ctor.newInstance(parameters);
                //clearReferences();

                return instance;
            } catch (ClassCastException | InstantiationException  | IllegalAccessException  | InvocationTargetException x) {
                throw new FailedToInstantiateBeanException(clazzName, x);
            }
        } else {
            throw new NoApplicableConstructorException(getValueType(), ctorParameters);
        }
    }

    /**
     * Create an array of Object that will constitute a parameter list.
     * Using the given list of AbstractProperty instances, create an Object array
     * whose members can be evaluated as instances of the given parameter types.
     *
     * @param orderedParameters
     * @param parameterTypes
     * @return
     * @throws ContextInitializationException
     */
    Object[] createArguments(final List<AbstractProperty> orderedParameters, final Class<?>[] parameterTypes)
            throws ContextInitializationException {

        if (orderedParameters == null || orderedParameters.size() == 0) {
            return new Object[0];
        }

        Object[] parameters = new Object[orderedParameters.size()];

        int index = 0;
        for (AbstractProperty argument : orderedParameters) {
            Class<?> parameterType = parameterTypes[index];

            parameters[index] = argument.getValue(parameterType);

            ++index;
        }
        return parameters;
    }

    /** Get the class of the referenced bean */
    public Class<T> getValueType() throws BeanClassNotFoundException {
        beanClassLock.lock();
        try {
            if (beanClass == null) {
                ClassLoader artifactClassLoader = getArtifactClassLoader();

                try {
                    if (artifactClassLoader != null) {
                        beanClass = (Class<T>) artifactClassLoader.loadClass(getClazzName());
                    } else {
                        beanClass = (Class<T>) Class.forName(getClazzName());
                    }
                } catch (ClassCastException | ClassNotFoundException x) {
                    throw new BeanClassNotFoundException(getClazzName(), x);
                }
            }
        } finally {
            beanClassLock.unlock();
        }

        return beanClass;
    }

    /**
     * Only works if the target class is a superclass result of a getValueType() call.
     *
     * @param targetClazz the target type
     * @param <S>
     * @return a cast of the getPropertyValue() result
     * @throws ContextInitializationException - if the target type is not the type or a super-type
     */
    @Override
    public <S> S getValue(final Class<S> targetClazz)
            throws ContextInitializationException {
        if (isResolvableAs(targetClazz)) {
            return targetClazz.cast(getValue());
        } else {
            throw new InvalidMorphTargetException(this, getValueType(), targetClazz);
        }
    }

    /**
     * Determine if the instance that will be (or has been) created from the BeanType
     * is assignable to a reference to the given class.
     *
     *
     * @param clazz the Class of a parameter for which the beanType resultant will be assigned
     * @return true, if an assignment of beanType resultant to a refernce of type clazz would succeed.
     * @throws BeanClassNotFoundException
     */
    @Override
    public boolean isResolvableAs(Class<?> clazz) throws ContextInitializationException {
        Class<?> actualArgumentType = getValueType();
        if (!clazz.isAssignableFrom(actualArgumentType)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "BeanFactoryReference{" +
                "id='" + getIdentifier() + '\'' +
                ", clazzName='" + getClazzName() + '\'' +
                '}';
    }

    /**
     * For bean instances marked as startable and with a start method,
     * instantiate this class as a Runnable wrapper so that the
     * active class has its own thread and its behavior does not
     * affect this framework.
     */
    private static class StartableInstance implements Runnable {
        private final String instanceIdentifier;
        private final String startMethodName;
        private final Object activeInstance;
        private Object result = null;
        private Throwable exception = null;

        StartableInstance(final String identifier, final String startMethodName, final Object activeInstance) {
            this.instanceIdentifier = identifier;
            this.startMethodName = startMethodName;
            this.activeInstance = activeInstance;
        }

        @Override
        public void run() {
            try {
                Method startMethod = this.activeInstance.getClass().getMethod(this.startMethodName);
                result = startMethod.invoke(this.activeInstance);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                exception = e;
                throw new RuntimeException(
                        new InvalidActiveClassAttributionException(this.instanceIdentifier, this.activeInstance.getClass().getName(), startMethodName, e)
                );
            }
        }
    }
}
