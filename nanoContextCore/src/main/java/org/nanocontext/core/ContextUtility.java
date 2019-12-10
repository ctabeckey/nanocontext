package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.utility.ExecutableSpecificityComparator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Static utility methods specific to NanoContext Core
 */
public final class ContextUtility {
    /** discourage construction */
    private ContextUtility() {}

    /**
     * Select the most specific constructor that will take the parameter types
     * @param beanClazz
     * @param orderedParameters
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public static <T> Constructor<T> selectConstructor(final Class<T> beanClazz, final List<AbstractProperty> orderedParameters)
            throws ContextInitializationException {
        SortedSet<Constructor<T>> sortedCtor = new TreeSet<Constructor<T>>(new ExecutableSpecificityComparator());

        for (Constructor<?> ctor : beanClazz.getConstructors()) {
            if (isApplicableConstructor(ctor, orderedParameters)) {
                sortedCtor.add((Constructor<T>) ctor);
            }
        }

        return sortedCtor.size() > 0 ? sortedCtor.first() : null;
    }

    /**
     *
     * @param factoryClass
     * @param factoryMethodName
     * @param ctorParameters
     * @param valueType
     * @param <T>
     * @return
     * @throws ContextInitializationException
     */
    public static <T> Method selectStaticFactoryMethod(
            final Class<?> factoryClass,
            final String factoryMethodName,
            final List<AbstractProperty> ctorParameters,
            final Class<T> valueType) throws ContextInitializationException {
        return selectFactoryMethod(factoryClass, Modifier.PUBLIC + Modifier.STATIC, factoryMethodName, ctorParameters, valueType);
    }

    /**
     *
     * @param factory
     * @param factoryMethodName
     * @param ctorParameters
     * @param valueType
     * @param <T>
     * @return
     * @throws ContextInitializationException
     */
    public static <T> Method selectFactoryMethod(
            final Object factory,
            final String factoryMethodName,
            final List<AbstractProperty> ctorParameters,
            final Class<T> valueType) throws ContextInitializationException {
        return selectFactoryMethod(factory.getClass(), Modifier.PUBLIC, factoryMethodName, ctorParameters, valueType);
    }

    /**
     *
     * @param clazz
     * @param modifiers
     * @param methodName
     * @param orderedParameters
     * @param valueType
     * @param <T>
     * @return
     * @throws ContextInitializationException
     */
    private static <T> Method selectFactoryMethod(
            final Class<?> clazz,
            final int modifiers,
            final String methodName,
            final List<AbstractProperty> orderedParameters,
            final Class<T> valueType) throws ContextInitializationException {
        SortedSet<Method> sortedCtor =
                new TreeSet<Method>(new ExecutableSpecificityComparator());

        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName())
                    && isApplicableFactoryMethod(method, orderedParameters, valueType)
                    && (method.getModifiers() & modifiers) == modifiers) {
                sortedCtor.add((Method) method);
            }
        }

        return sortedCtor.size() > 0 ? sortedCtor.first() : null;
    }

    /**
     * Given a constructor and an ordered list of parameters (from the context XML),
     * determine whether the constructor could be called to instantiate the bean.
     *
     * @param ctor
     * @param parameters
     * @return
     */
    public static boolean isApplicableConstructor(final Constructor<?> ctor, final List<AbstractProperty> parameters)
            throws ContextInitializationException {
        // Iterate through each parameter in the given constructor
        // and determine whether the correlated ordered argument can be used in that parameter
        return isApplicableParameters(parameters, ctor.getParameterTypes());
    }

    /**
     * Given a method, an ordered list of parameters (from the context XML), and an expected return type,
     * determine whether the method could be called to instantiate the bean.
     *
     * @param method
     * @param parameters
     * @param returnType
     * @return
     */
    public static boolean isApplicableFactoryMethod(final Method method, final List<AbstractProperty> parameters, final Class<?> returnType)
            throws ContextInitializationException {
        if (method.getReturnType() == null && returnType == null
                || returnType.isAssignableFrom(method.getReturnType()) ) {

            // Iterate through each parameter in the given method
            // and determine whether the correlated ordered argument can be used in that parameter
            return isApplicableParameters(parameters, method.getParameterTypes());
        } else {
            return false;
        }
    }

    /**
     *
     * @param parameters
     * @param parameterTypes
     * @return
     * @throws ContextInitializationException
     */
    public static boolean isApplicableParameters(List<AbstractProperty> parameters, Class<?>[] parameterTypes) throws ContextInitializationException {
        if ( parameters == null && parameterTypes == null) {
            return true;
        }

        int parameterCount = parameters == null ? 0 : parameters.size();
        int parameterTypeCount = parameterTypes == null ? 0 : parameterTypes.length;

        if ( parameterCount != parameterTypeCount) {
            return false;
        }

        for (int index = 0; index < parameterTypes.length; ++index) {
            // Actual parameter type is the type of the current-index parameter
            // within the constructor.
            Class<?> constructorParameterType = parameterTypes[index];

            AbstractProperty parameter = parameters.get(index);

            if (!parameter.isResolvableAs(constructorParameterType)) {
                return false;
            }
        }

        return true;
    }
}
