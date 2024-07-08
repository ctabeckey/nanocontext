package org.nanocontext.annotationsprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The root of the data model used to generate the ContextFactory.
 * This class, and the classes referenced within it, must be understandable to
 * FreeMarker, which is why they extends a Map class.
 */
class ContextFactoryDataModel extends HashMap<String, Object> {
    public static final String DEFAULT_PACKAGE_NAME = "org.nanocontext.application";
    public static final String DEFAULT_CONTEXT_FACTORY_NAME = "NanoContextFactory";

    public static final String TEMPLATE_PACKAGE_NAME = "packageName";
    public static final String TEMPLATE_CONTEXT_FACTORY_NAME = "contextFactoryName";
    public static final String TEMPLATE_BEANS = "beans";


    protected List<ResourceType> properties;
    protected List<ResourceType> artifact;
    protected List<BeanType> bean;

    public ContextFactoryDataModel() {
        this.put(TEMPLATE_PACKAGE_NAME, DEFAULT_PACKAGE_NAME);
        this.put(TEMPLATE_CONTEXT_FACTORY_NAME, DEFAULT_CONTEXT_FACTORY_NAME);
    }

    public String getPackageName() {
        return this.get(TEMPLATE_PACKAGE_NAME).toString();
    }

    public void setPackageName(final String packageName) {
        if (packageName != null && packageName.length() > 0)
            this.put(TEMPLATE_PACKAGE_NAME, packageName);
    }

    public String getContextFactoryName() {
        return this.get(TEMPLATE_CONTEXT_FACTORY_NAME).toString();
    }

    public void setContextFactoryName(final String contextFactoryName) {
        if (contextFactoryName != null && contextFactoryName.length() > 0)
            this.put(TEMPLATE_CONTEXT_FACTORY_NAME, contextFactoryName);
    }

    public void setBeans(final List<BeanType> beans) {
        if (beans != null) {
            this.put(TEMPLATE_BEANS, beans);
        } else {
            this.remove(TEMPLATE_BEANS);
        }
    }

    public List<BeanType> getBeans() {
        if (! this.containsKey(TEMPLATE_BEANS)) {
            this.put(TEMPLATE_BEANS, new ArrayList<BeanType>());
        }
        return (List<BeanType>) this.get(TEMPLATE_BEANS);
    }

    public List<ResourceType> getProperties() {
        if (properties == null) {
            properties = new ArrayList<ResourceType>();
        }
        return this.properties;
    }

    public List<ResourceType> getArtifact() {
        if (artifact == null) {
            artifact = new ArrayList<ResourceType>();
        }
        return this.artifact;
    }

}
