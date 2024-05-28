package org.nanocontext.xml.subjects;

import java.util.Properties;

/**
 * Created by cbeckey on 4/7/17.
 */
public class BeanWithPropertiesConstructorArg {
    private final Properties properties;

    public BeanWithPropertiesConstructorArg(final Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPropertyValue(final String key) {
        return this.properties.getProperty(key);
    }
}
