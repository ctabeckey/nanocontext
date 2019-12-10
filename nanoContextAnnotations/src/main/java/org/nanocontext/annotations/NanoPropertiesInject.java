package org.nanocontext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation on a constructor parameter that will cause the referenced
 * resource to be loaded as a Properties instance and injected as the
 * constructor parameter.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface NanoPropertiesInject {
    String resourceLocation();
}
