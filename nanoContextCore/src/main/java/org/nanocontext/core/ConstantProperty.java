package org.nanocontext.core;

import org.nanocontext.utility.exceptions.CannotCreateObjectFromStringException;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.core.exceptions.InvalidMorphTargetException;
import org.nanocontext.utility.Utility;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cbeckey on 3/15/16.
 */
public class ConstantProperty<T> extends AbstractProperty<T> {
    private final String rawValue;
    private final Class<T> valueType;
    private ReentrantLock instantiationLock = new ReentrantLock();
    private T value;

    /**
     *
     * @param rawValue
     * @param valueType
     */
    public ConstantProperty(final Context context, final String rawValue, final Class<T> valueType) throws ContextInitializationException {
        super(context);
        this.rawValue = Utility.isPropertyReference(rawValue) ?
                resolvePropertyValue(Utility.extractKeyFromPropertyReference(rawValue)) :
                rawValue;
        this.valueType = valueType;
    }

    /** */
    @Override
    public T getValue() throws ContextInitializationException {
        instantiationLock.lock();
        try {
            if (value == null) {
                try {
                    value = Utility.createInstanceFromStringValue(this.valueType, this.rawValue, true);
                } catch (CannotCreateObjectFromStringException e) {
                    throw new ContextInitializationException(e);
                }
            }
        } finally {
            instantiationLock.unlock();
        }

        return value;
    }

    /**
     * Get the value as the given type.
     * This method will do conversion, valueOf, instantiation, etc as it needs to.
     * NOTE: unlike isResolvableAs(), which is restricted to primitive and java.lang.*
     * classes, this method will try to convert to whatever class is given. Exceptions
     * may be thrown if the conversion cannot be accomplished. In practice the target class
     * must have a static valueOf(String) method or a constructor expecting a single String
     * parameter.
     *
     * @see #isResolvableAs(Class)
     *
     * @param targetClazz the target type
     * @param <S> the Type of the target type
     * @return an instance of the constant value as the given type
     * @throws ContextInitializationException - usually if the conversion cannot be done
     */
    public <S> S getValue(final Class<S> targetClazz)
            throws ContextInitializationException {
        if (targetClazz == null || targetClazz.equals(getValueType())) {
            return (S)getValue();
        } else {
            if (isResolvableAs(targetClazz)) {
                try {
                    return Utility.createInstanceFromStringValue(targetClazz, this.rawValue, false);
                } catch (CannotCreateObjectFromStringException e) {
                    throw new ContextInitializationException(e);
                }
            }
            throw new InvalidMorphTargetException(this, getValueType(), targetClazz);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getValueType() {
        return this.valueType;
    }

    /**
     * Because of the order of execution, all constants are created as String values.
     * When the constructor of a Bean is called, the type may be morphed to make a compatible value
     * with a constructor argument.
     *
     * NOTE: the targetValueType must be either a primitive, Class, or a class that is part of
     * the java.lang package. This class explicitly restricts its getPropertyValue() result to one
     * of those types.
     *
     * @param targetValueType
     * @return
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResolvableAs(Class<?> targetValueType) throws ContextInitializationException {
        try {
            Utility.createInstanceFromStringValue(targetValueType, this.rawValue, true);
            return true;
        } catch(CannotCreateObjectFromStringException ccofsX) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ConstantProperty{" +
                "valueType=" + valueType.toString() +
                ", rawValue='" + rawValue.toString() + '\'' +
                '}';
    }
}
