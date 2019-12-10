package org.nanocontext.xml.exceptions;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.BeanType;

/**
 * Created by cbeckey on 2/4/16.
 */
public class SparseArgumentListDetectedException extends ContextInitializationException {
    private static final String createMessage(final BeanType beanType) {
        return String.format("Sparse argument specified in constructor for %s{%s}",
                beanType.getClazz(),
                beanType.getId()
        );
    }

    public SparseArgumentListDetectedException(final BeanType beanType) {
        super(createMessage(beanType));
    }
}
