package org.nanocontext.utility;

import org.nanocontext.utility.exceptions.CannotCreateObjectFromStringException;
import org.nanocontext.utility.exceptions.UnknownCollectionTypeException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;

/**
 * A collection of utility methods that are not specific to NanoContext core.
 *
 * A Note on Naming:
 * A parameter is part of the method signature,
 * the argument is what is passed at runtime.
 */
public class Utility {
    /** Should the name of the valueOf methods ever change, we're ready */
    public static final String VALUE_OF_METHOD_NAME = "valueOf";
    public static final String PREFIX = "${";
    public static final String SUFFIX = "}";

    /** used to find valueOf(String) methods */
    private final static Class<?>[] SINGLE_STRING_PARAMETER = new Class<?>[]{String.class};

    /** The packages in this array are treated specially when creating an instance from a String value */
    private final static Package[] CORE_PACKAGES = new Package[] {
            java.lang.Class.class.getPackage(),
    };

    /** Prevent instantiation */
    private Utility() {}

    /**
     * Return true if the class is in the core (java.lang) package
     * @param clazz the class to evaluate
     * @return true if the class is in the core (java.lang) package, else false
     */
    public final static boolean isClassInCorePackage(final Class<?> clazz) {
        if (clazz != null) {
            Package clazzPackage = clazz.getPackage();
            for (Package corePackage : CORE_PACKAGES) {
                if (corePackage.equals(clazzPackage)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param clazz
     * @param value
     * @param <T>
     * @return
     * @throws CannotCreateObjectFromStringException
     */
    public static <T> T createInstanceFromStringValue(final Class<T> clazz, final String value, boolean coreClassesOnly)
            throws CannotCreateObjectFromStringException {
        Object[] parameters = new Object[]{value};

        // special handling for String target types
        if (String.class.equals(clazz)) {
            return (T)value;
        }

        // special handling for primitive types
        if (clazz.isPrimitive()) {
            if (byte.class.equals(clazz)) {
                return (T) (new Byte(value));
            } else if (short.class.equals(clazz)) {
                return (T) (new Short(value));
            } else if (int.class.equals(clazz)) {
                return (T) (new Integer(value));
            } else if (long.class.equals(clazz)) {
                return (T) (new Long(value));
            } else if (float.class.equals(clazz)) {
                return (T) (new Float(value));
            } else if (double.class.equals(clazz)) {
                return (T) (new Double(value));
            } else if (char.class.equals(clazz)) {
                return (T) new Character(value.charAt(0));
            }
        }

        // special handling for Class target types
        if (Class.class.equals(clazz)) {
            try {
                return (T)Class.forName(value);
            } catch (ClassNotFoundException x) {
                throw new CannotCreateObjectFromStringException(clazz, x);
            }
        }

        // special handling for core classes used for creating ConstantProperty
        // where it should not create a class of a type other than the core classes
        if (coreClassesOnly && ! isClassInCorePackage(clazz)) {
            throw new CannotCreateObjectFromStringException(clazz);
        }

        try {
            // look for a 'valueOf' method
            Method valueOfMethod = clazz.getMethod(VALUE_OF_METHOD_NAME, SINGLE_STRING_PARAMETER);
            return (T)valueOfMethod.invoke(null, parameters);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException x) {
            // look for a T(String) constructor
            try {
                Constructor ctor = clazz.getConstructor(String.class);
                return (T)ctor.newInstance(parameters);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new CannotCreateObjectFromStringException(clazz, x);
            }
        }
    }

    /**
     *
     * @param componentType
     * @param result
     * @return
     */
    public static Object createTypedArray(final Class<?> componentType, final List result) {
        Object arrayResult = Array.newInstance(componentType, result.size());
        int index = 0;
        for (Object element : result) {
            Array.set(arrayResult, index++, element);
        }
        return arrayResult;
    }

    /**
     * Try to determine the type of the elements from the parameterType
     * For arrays this is dependable, for Collection it depends on compiler options.
     *
     * @param parameterType
     * @return
     * @throws UnknownCollectionTypeException
     */
    public static Class<?> extractElementType(final Class<?> parameterType)
            throws UnknownCollectionTypeException {
        Class<?> componentType = null;

        if (parameterType.isArray() && !parameterType.getComponentType().isArray()) {
            // get the one and only element type
            componentType = parameterType.getComponentType();
        } else if (Collection.class.isAssignableFrom(parameterType)) {
            TypeVariable<? extends Class<?>>[] typeParameters = parameterType.getTypeParameters();
            if (typeParameters == null || typeParameters.length == 0) {
                // don't know, assume Object
                componentType = Object.class;
            } else if (typeParameters.length > 1){
                // more than one Type parameter, can't handle it !
                throw new UnknownCollectionTypeException(parameterType);
            } else {
                // get the one and only generic type declaration
                Type[] upperBounds = typeParameters[0].getBounds();
                // the following should be upperBounds[0].getTypeName()
                // needs JDK 8
                String typeName = upperBounds[0].toString();
                try {
                    componentType = Class.forName(typeName);
                } catch (ClassNotFoundException e) {
                    throw new UnknownCollectionTypeException(parameterType);
                }
            }
        }

        // will return null if the paremeterType is not a collection or array
        return componentType;
    }

    // =======================================================================================================================
    //
    // =======================================================================================================================
    public static boolean isPropertyReference(final String key) {
        if (key == null) {
            return false;
        }

        boolean result = key.startsWith(PREFIX) && key.endsWith(SUFFIX);
        return result;
    }

    public static String extractKeyFromPropertyReference(final String key) {
        if (key == null) {
            return null;
        }

        String result = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
        return result;
    }

    // =======================================================================================================================
    //
    // =======================================================================================================================
    public static int compareParameterTypes(final Class<?>[] method1Parameters, final Class<?>[] method2Parameters) {
        if (method1Parameters.length < method2Parameters.length) {
            return -1;
        } else if (method1Parameters.length > method2Parameters.length) {
            return 1;
        }


        for (int index = 0; index < method1Parameters.length; ++index) {
            if (method1Parameters[index].equals(method2Parameters[index])) {
                continue;
            } else if (method1Parameters[index].isAssignableFrom(method2Parameters[index])) {
                return 2;
            } else if (method2Parameters[index].isAssignableFrom(method1Parameters[index])) {
                return -2;

            } else if (String.class.equals(method1Parameters[index])
                    && !String.class.equals(method2Parameters[index])) {
                // one of two odd cases where a constructor may take a type that can be constructed
                // using a valueOf(String) method. Constructors taking a String should be sorted later
                // than an otherwise equivalent constructor (taking an Integer for instance)
                return 3;
            } else if (String.class.equals(method2Parameters[index])
                    && !String.class.equals(method1Parameters[index])) {
                return -3;
            }
        }
        return 0;
    }
}
