package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;

import java.util.Objects;

/**
 * The abstract super-class for all AbstractProperty classes that are referencable by name.
 * The known derivations are:
 * @see AbstractBeanInstanceFactory
 * @see PrototypeBeanInstanceFactory
 * @see SingletonBeanInstanceFactory
 * @see PreresolvedBean
 */
public abstract class AbstractReferencableProperty<T>
extends AbstractProperty<T>
implements Comparable<AbstractReferencableProperty> {
    private final String identifier;

    /**
     * Required pass-through constructor
     * @param context
     */
    public AbstractReferencableProperty(final Context context, final String identifier) {
        super(context);
        if (identifier == null)
            throw new IllegalArgumentException("Instances of AbstractReferencableProperty must be constructed with a non-null identifier.");

        this.identifier = identifier;
    }

    /**
     *
     * @return
     */
    public String getIdentifier() {
        return this.identifier;
    };

    /**
     *
     */
    public abstract void initialize() throws ContextInitializationException;

    // =====================================================================================
    // Implementations of compareTo, equals and hashCode based on the identifier field
    // =====================================================================================
    @Override
    public int compareTo(AbstractReferencableProperty o) {
        return this.getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractReferencableProperty<?> that = (AbstractReferencableProperty<?>) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
