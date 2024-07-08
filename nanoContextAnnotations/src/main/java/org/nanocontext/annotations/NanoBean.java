package org.nanocontext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as manageable by NanoContext, optionally specifies an
 * identifier to be used to reference instances of this bean.
 * By default, beans are created as Singleton, that is one instance in the context.
 * Beans may be marked as Prototype, that is a new instance will be created each time an
 * injection reference is made.
 * active - An "active" bean is one that has its own Thread of execution. If active is set then then
 * the Bean implementation must either:
 * include a public void run() method
 * include a public void method taking no arguments and having the name specified in the activate property.
 * activate -
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NanoBean {
    String scope() default "singleton";
    String identifier() default "";
    boolean lazyLoad() default false;
    boolean active() default false;
    String activate() default "run";
    String context() default "default";
}
