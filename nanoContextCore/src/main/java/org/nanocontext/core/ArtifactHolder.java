package org.nanocontext.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds references to late-loaded artifacts.
 */
public class ArtifactHolder {
    private final String identifier;
    private final ClassLoader parentClassLoader;
    private URLClassLoader classLoader;
    private final URI resourceIdentifier;
    private boolean loaded = false;

    private final ReentrantLock loadedLock = new ReentrantLock();
    private final Condition notLoaded  = loadedLock.newCondition();

    private final static Logger LOGGER = LoggerFactory.getLogger(ArtifactHolder.class);

    /**
     *
     * @param identifier
     * @param resourceIdentifier
     * @param parentClassLoader
     * @throws MalformedURLException
     */
    public ArtifactHolder(final String identifier, final URI resourceIdentifier, final ClassLoader parentClassLoader)
            throws MalformedURLException {
        this.identifier = identifier;
        this.resourceIdentifier = resourceIdentifier;
        this.parentClassLoader = parentClassLoader;

        URL artifactLocation = this.resourceIdentifier.toURL();

        if ("http".equals(artifactLocation.getProtocol()) || "https".equals(artifactLocation.getProtocol())) {
            String tempPath = System.getenv("TMPDIR");
            File tempDirectory = new File(tempPath);
            LOGGER.debug("Application "
                    + (tempDirectory.canRead() ? "CAN read" : "CANNOT read")
                    + " and "
                    + (tempDirectory.canWrite() ? "CAN write" : "CANNOT write")
                    + " to "
                    + tempPath
            );
        }

        this.classLoader = URLClassLoader.newInstance(new URL[]{artifactLocation}); // , parentClassLoader, null
    }


    public String getIdentifier() {
        return identifier;
    }

    /**
     *
     * @return
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactHolder that = (ArtifactHolder) o;
        return Objects.equals(identifier, that.identifier) &&
                Objects.equals(resourceIdentifier, that.resourceIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, resourceIdentifier);
    }
}
