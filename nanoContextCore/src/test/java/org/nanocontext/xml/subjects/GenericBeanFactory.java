package org.nanocontext.xml.subjects;

/**
 * Created by cbeckey on 1/23/17.
 */
public class GenericBeanFactory {
    public static BeanWithStaticConstructor createBean() {
        return new BeanWithStaticConstructor();
    }

    public BeanWithStaticConstructor createBeanInstance() {
        return new BeanWithStaticConstructor();
    }

}
