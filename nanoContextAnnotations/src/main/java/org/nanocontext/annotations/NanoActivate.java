package org.nanocontext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method within a NanoBean which is called to activate the bean.
 * This annotation is only significant when a Class is marked with NanoBean
 * and the active property is true.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface NanoActivate {
}
