package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.utility.Utility;
import org.nanocontext.utility.references.Derivations;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A very small and limited function IoC Context.
 * Implements simple hierarchical delegation, whereas if a bean cannot be found
 * within this context it will delegate to a parent context.
 */
public class Context {
    /**
     * The bean references that make up this context.
     * Immutable once created.
     * NOTE: natural ordering of AbstractReferencableProperty is by identifier
     */
    private TreeSet<AbstractReferencableProperty> referencableProperties;
    private Set<ArtifactHolder> artifacts;
    private Set<PropertiesHolder> properties;
    private final ThreadGroup contextThreadGroup;
    private final Context parent;

    /**
     * Package level scope is intentional, only the XMLContextFactory should
     * construct instances of this Class.
     */
    public Context() {
        this(null);
    }

    /**
     * Package level scope is intentional, only the XMLContextFactory should
     * construct instances of this Class.
     */
    public Context(final Context parent) {
        this.contextThreadGroup = new ThreadGroup("ContextThreadGroup");
        this.parent = parent;
    }

    /**
     * Get the parent context if there is one, else return null.
     *
     * @return
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Returns the ThreadGroup under which Threads for Active beans will be
     * members of.
     * @return
     */
    public ThreadGroup getContextThreadGroup() {
        return contextThreadGroup;
    }

    /**
     * Package level scope is intentional, only the XMLContextFactory should
     * call this method.
     *
     * @param artifacts
     */
    public void setArtifacts(Set<ArtifactHolder> artifacts) {
        this.artifacts = Collections.unmodifiableSet(artifacts);
    }

    /**
     * Package level scope is intentional, only the XMLContextFactory should
     * call this method.
     *
     * @param properties
     */
    public void setProperties(Set<PropertiesHolder> properties) {
        this.properties = properties;
    }

    /**
     * This method should only be called by a context factory implementation.
     *
     * @param referencableProperties
     */
    public void setContextObjectsMap(final Set<AbstractReferencableProperty> referencableProperties) {
        this.referencableProperties = new TreeSet<>();
        this.referencableProperties.addAll(referencableProperties);
    }

    /**
     *
     * @param key
     */
    public String resolvePropertyValue(final String key) throws ContextInitializationException {
        String value = null;
        if (key != null) {
            if (this.properties != null) {
                for (PropertiesHolder propertiesHolder : this.properties) {
                    value = propertiesHolder.getPropertyValue(key);
                    if (value != null ) {
                        if(Utility.isPropertyReference(value)) {
                            value = Utility.extractKeyFromPropertyReference(key);
                            value = resolvePropertyValue(value);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return value;
    }

    /**
     * @param identifier
     * @return
     */
    public ArtifactHolder getArtifactHolder(final String identifier) {
        if (this.artifacts != null && identifier != null) {
            for (ArtifactHolder holder : this.artifacts) {
                if (identifier.equals(holder.getIdentifier())) {
                    return holder;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param identifier
     * @return
     */
    public PropertiesHolder getPropertiesHolder(final String identifier) {
        if (this.properties != null && identifier != null) {
            for (PropertiesHolder holder : this.properties) {
                if (identifier.equals(holder.getIdentifier())) {
                    return holder;
                }
            }
        }
        return null;
    }

    /**
     * Get a Bean from the context whose type most closely matches the given type.
     * Most closely matches is defined as the given type is the same or that the given type is
     * a superclass of the given type and that it is the most specific superclass
     * of the given type in the context.
     *
     * @param beanClass the desired bean type
     * @param <T>       The requested bean type
     * @return the bean of the requested type or derivation of the given type
     */
    public <T> T getBean(final Class<T> beanClass)
            throws ContextInitializationException {
        if (beanClass == null)
            return null;

        // find the most specific bean in the context by type
        int minDistance = Integer.MAX_VALUE;
        AbstractBeanInstanceFactory<T> selectedBeanReference = null;

        for (AbstractReferencableProperty<?> property : referencableProperties) {
            int beanDistance = Derivations.instanceDistance(property.getValueType(), beanClass);

            if (beanDistance < minDistance) {
                minDistance = beanDistance;
                selectedBeanReference = (AbstractBeanInstanceFactory<T>) property;
            }
        }

        T bean = selectedBeanReference == null ? null : (T) selectedBeanReference.getValue();

        // delegate to the parent if the bean was not found
        if (bean == null && this.parent != null) {
            bean = this.parent.getBean(beanClass);
        }

        return bean;
    }

    /**
     * @param id
     * @param beanClass
     * @param <T>
     * @return
     */
    public <T> T getBean(final String id, final Class<T> beanClass)
            throws ContextInitializationException {
        if (id == null)
            return null;

        AbstractProperty<T> beanReference = getBeanReference(id);
        T bean = beanReference.getValue();

        // validate that the bean is of the expected class or that the class was not specified
        bean = beanClass == null || beanClass.isInstance(bean) ? bean : null;

        // delegate to the parent if the bean was not found
        if (bean == null && this.parent != null) {
            bean = this.parent.getBean(beanClass);
        }

        return bean;
    }

    /**
     *
     * @param id
     * @param beanClass
     * @return
     * @throws ContextInitializationException
     */
    public AbstractProperty getBeanReference(final String id, final Class<?> beanClass)
            throws ContextInitializationException {
        if (beanClass == null || id == null)
            return null;

        AbstractProperty<?> beanReference = getBeanReference(id);
        Class<?> beanType = beanReference.getValueType();
        return beanClass.equals(beanType) ? beanReference : null;
    }

    /**
     * Recursively search for a bean by identifier in this context
     * and, if not found, in the ancestor contexts
     *
     * @param identifier
     * @return
     * @throws ContextInitializationException
     */
    public AbstractProperty getBeanReference(final String identifier)
            throws ContextInitializationException {
        AbstractProperty ap = getReferencableProperty(identifier);

        if (ap == null && this.parent != null) {
            ap = this.parent.getBeanReference(identifier);
        }

        return ap;
    }

    /**
     * Finds an AbstractReferencableProperty in the context by identifier.
     *
     * @param identifier the identifier of the bean to find
     * @return returns the referenced bean or null if not found
     */
    private AbstractReferencableProperty getReferencableProperty(final String identifier) {
        if (identifier != null)
            for (AbstractReferencableProperty<?> abstractReferencableProperty : this.referencableProperties)
                if (abstractReferencableProperty.getIdentifier().equals(identifier))
                    return abstractReferencableProperty;

        return null;
    }

    /**
     *
     * @param referencedPropertiesIdentifier
     * @return
     */
    public PropertiesHolder getPropertiesReference(final String referencedPropertiesIdentifier) {
        if (referencedPropertiesIdentifier != null) {
            for (PropertiesHolder propertiesHolder : properties) {
                if (referencedPropertiesIdentifier.equals(propertiesHolder.getIdentifier())) {
                    return propertiesHolder;
                }
            }
        }

        return null;
    }



    // ========================================================================================
    // A simple mechanism for managing ActiveBean instances.
    // ========================================================================================
    private List<Runnable> activeBeans = new ArrayList<>();

    void registerActiveBean(Runnable activeBean) {
        activeBeans.add(activeBean);
    }

    /**
     * Called by the XMLContextFactory after all of the bean factories have been created.
     * This must create the beans that are marked as lazy-load, which will
     * also start the active beans.
     */
    public void initialize() throws ContextInitializationException {
        for (PropertiesHolder propertiesHolder : this.properties) {
            propertiesHolder.initialize();
        }

        for (AbstractReferencableProperty abstractReferencableProperty : referencableProperties) {
            abstractReferencableProperty.initialize();
        }
    }

    /**
     * Called by the client code when the context should shut down.
     *
     */
    public void shutdown() {
        for (Runnable activeBean : activeBeans) {
            if (ActiveBean.class.isAssignableFrom(activeBean.getClass())) {
                ((ActiveBean)activeBean).shutdown();
            }
        }
    }
}