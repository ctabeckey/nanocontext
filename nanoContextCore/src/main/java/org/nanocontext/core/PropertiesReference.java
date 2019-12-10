package org.nanocontext.core;

import org.nanocontext.core.exceptions.BeanClassNotFoundException;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.core.exceptions.InvalidMorphTargetException;
import org.nanocontext.utility.Utility;

import java.util.Properties;

/**
 * A placeholder that is stuffed into the context when a ref element is encountered.
 * This instance will be replaced on the first use of the bean with a reference to
 * the actual instance.
 */
public final class PropertiesReference extends AbstractProperty<Properties> {
    /** */
    final private String referencedPropertiesId;

    /**
     * @param context
     * @param referencedPropertiesId
     */
    public PropertiesReference(final Context context, final String referencedPropertiesId)
            throws ContextInitializationException {
        super(context);

        this.referencedPropertiesId = Utility.isPropertyReference(referencedPropertiesId) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(referencedPropertiesId)) :
                referencedPropertiesId;
    }

    /**
     *
     * @return
     */
    public String getReferencedPropertiesIdentifier() {
        return this.referencedPropertiesId;
    }

    /**
     *
     * @return
     * @throws ContextInitializationException
     */
    private PropertiesHolder getReferencedProperties()
            throws ContextInitializationException {

        // search through the hierarchy of parent contexts, starting with the current
        // context, until the bean reference is found
        AbstractProperty beanRef = null;
        for (Context ctx = getContext(); ctx != null && beanRef == null; ctx = ctx.getParent()) {
            beanRef = ctx.getPropertiesReference(getReferencedPropertiesIdentifier());
        }

        return beanRef instanceof PropertiesHolder ? (PropertiesHolder)beanRef : null;
    }
    /**
     * Get the value from the referenced bean, morphing types if needed.
     * @return the bean value
     */
    public Properties getValue() throws ContextInitializationException {
        PropertiesHolder propertiesRef = getReferencedProperties();
        if (propertiesRef != null) {
            return propertiesRef.getValue();
        }
        return null;
    }

    /**
     * Get the value as the given type.
     * This method should do conversion, valueOf, instantiation, etc as it needs to.
     *
     * @param targetClazz the target type
     * @return an instance of the constant value as the given type
     * @throws ContextInitializationException - usually if the conversion cannot be done
     * @see #isResolvableAs(Class)
     */
    @Override
    public <S> S getValue(Class<S> targetClazz) throws ContextInitializationException {
        if (isResolvableAs(targetClazz)) {
            return targetClazz.cast(getValue());
        } else {
            throw new InvalidMorphTargetException(this, getValueType(), targetClazz);
        }
    }

    /**
     *
     * @return
     * @throws BeanClassNotFoundException
     */
    public Class<Properties> getValueType() throws ContextInitializationException {
        return Properties.class;
    }

    /**
     * Returns true if the property can be resolved as the given type
     * @param clazz
     */
    @Override
    public boolean isResolvableAs(Class<?> clazz) throws ContextInitializationException {
        return clazz == null ? false : clazz.isAssignableFrom(Properties.class);
    }

}
