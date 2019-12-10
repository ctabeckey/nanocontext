package org.nanocontext.core;

import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.core.exceptions.GenericContextInitializationException;
import org.nanocontext.core.exceptions.PropertiesLoadException;
import org.nanocontext.utility.url.URLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds references to late-loaded properties.
 */
public class PropertiesHolder extends AbstractReferencableProperty<Properties>{
    private final String identifier;
    private final URL propertiesLocation;
    private final URI resourceIdentifier;
    private boolean loaded = false;

    private Properties properties = null;

    private final ReentrantLock loadedLock = new ReentrantLock();
    private final Condition notLoaded  = loadedLock.newCondition();

    /**
     *
     * @param identifier
     * @param resourceIdentifier
     * @throws MalformedURLException
     */
    public PropertiesHolder(final Context context, final String identifier, final URI resourceIdentifier)
            throws MalformedURLException {
        super(context, identifier);

        this.identifier = identifier;
        this.resourceIdentifier = resourceIdentifier;

        this.propertiesLocation = URLFactory.create(this.resourceIdentifier.toString());

        if ("http".equals(propertiesLocation.getProtocol()) || "https".equals(propertiesLocation.getProtocol())) {
            String tempPath = System.getenv("TMPDIR");
            File tempDirectory = new File(tempPath);
            System.out.println("Application "
                    + (tempDirectory.canRead() ? "CAN read" : "CANNOT read")
                    + " and "
                    + (tempDirectory.canWrite() ? "CAN write" : "CANNOT write")
                    + " to "
                    + tempPath
            );
        }
    }

    @Override
    public boolean isResolvableAs(Class clazz) throws ContextInitializationException {
        return Properties.class.isAssignableFrom(clazz);
    }

    @Override
    public void initialize() throws ContextInitializationException {
        if (! this.loaded) {
            this.loadedLock.lock();

            InputStream propertyStream = null;
            try {
                propertyStream = this.propertiesLocation.openStream();
                if (propertyStream == null) {
                    throw new PropertiesLoadException(this.propertiesLocation);
                }
                this.properties = new Properties();
                this.properties.load(propertyStream);

            } catch (IOException ioX) {
                throw new PropertiesLoadException(this.propertiesLocation, ioX);

            } finally {
                try {propertyStream.close();}
                catch(Throwable t){}        // eat any exceptions, they are secondary to the real problem
                this.loaded = true;         // don't retry
                this.loadedLock.unlock();
            }
        }
    }

    @Override
    public Properties getValue() throws ContextInitializationException {
        return this.properties;
    }

    @Override
    public <S> S getValue(Class<S> targetClazz) throws ContextInitializationException {
        try {
            return targetClazz == null ? null : (S) this.properties;
        } catch(ClassCastException ccX) {
            throw new GenericContextInitializationException(String.format("Property cannot be cast as [%s]", targetClazz.getSimpleName()));
        }
    }

    @Override
    public Class<?> getValueType() throws ContextInitializationException {
        return Properties.class;
    }

    /**
     *
     * @param key
     * @return
     */
    public String getPropertyValue(final String key) throws ContextInitializationException {
        if (! this.loaded) {
            initialize();
        }

        return this.properties.getProperty(key);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getPropertyValue(final String key, final String defaultValue) throws ContextInitializationException {
        if (! this.loaded) {
            initialize();
        }

        return this.properties.getProperty(key, defaultValue);
    }

    /**
     *
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertiesHolder that = (PropertiesHolder) o;
        return Objects.equals(identifier, that.identifier) &&
                Objects.equals(resourceIdentifier, that.resourceIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, resourceIdentifier);
    }
}
