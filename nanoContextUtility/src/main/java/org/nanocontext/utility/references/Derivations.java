package org.nanocontext.utility.references;

/**
 *
 */
public class Derivations {
    /**
     * @param subject
     * @param baseClazz
     * @return
     */
    public static int instanceDistance(final Object subject, final Class<?> baseClazz) {
        if (subject == null) {
            throw new IllegalArgumentException("'subject' is null and must not be.");
        }
        return distance(subject.getClass(), baseClazz);
    }

    /**
     * @param subjectClazz
     * @param baseClazz
     * @return
     */
    public static int distance(final Class<?> subjectClazz, final Class<?> baseClazz) {
        if (subjectClazz == null) {
            throw new IllegalArgumentException("'subjectClazz is null and must not be,");
        }
        if (baseClazz == null) {
            throw new IllegalArgumentException("'baseClazz is null and must not be,");
        }
        if (baseClazz.equals(subjectClazz)) {
            return 0;
        }
        if (!baseClazz.isAssignableFrom(subjectClazz)) {
            return Integer.MAX_VALUE;
        }

        int parentDistance = distance(subjectClazz.getSuperclass(), baseClazz);
        if (parentDistance == Integer.MAX_VALUE) {
            for (Class<?> intf : subjectClazz.getInterfaces()) {
                parentDistance = Math.min(parentDistance, distance(intf, baseClazz));
            }
        }

        return parentDistance + 1;
    }
}
