package org.nanocontext.annotationsprocessor;

import java.util.HashMap;

/**
 *
 */
class ConstructorArgType extends HashMap<String, Object> {

    protected BeanType bean;
    protected String value;
    protected ListType list;
    protected ReferenceType ref;
    protected PropertiesReferenceType propertiesRef;
    protected Integer index;
}
