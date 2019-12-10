package org.nanocontext.xml.subjects;

/**
 * Created by cbeckey on 3/14/16.
 */
public class BeanWithStaticConstructor {
    public static BeanWithStaticConstructor createBean() {
        return new BeanWithStaticConstructor();
    }

}
