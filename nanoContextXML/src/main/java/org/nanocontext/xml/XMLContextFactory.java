package org.nanocontext.xml;

import org.nanocontext.core.*;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.exceptions.InvalidArtifactSyntaxException;
import org.nanocontext.xml.exceptions.InvalidPropertiesSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class XMLContextFactory implements ContextFactory {
    /** */
    private final static Logger LOGGER = LoggerFactory.getLogger(XMLContextFactory.class);

    /** JAXB Context is created on demand */
    private JAXBContext jaxbContext = null;

    /**
     * The class loader to use for loading the context beans from
     */
    private ClassLoader classLoader;

    /**
     * The parent of the context to create.
     * A parent is delegated to if a bean cannot be found in the
     * current context.
     */
    private Context parent;

    // ========================================================================================
    // The components of the XMLContextFactory that populate the Context
    // ========================================================================================

    /** Contains all of the artifact references */
    private Set<ResourceType> artifacts = new HashSet<>();

    /** Contains all of the properties references */
    private Set<ResourceType> properties = new HashSet<>();

    /**
     * Contains BeanType instances, usually read from an XML resource.
     * Note that this collection MUST be kept in order from the source document
     * because it is up to the source document to define references in order
     * that avoids the need for forward reference resolution.
     */
    private List<BeanType> beanTypes = new ArrayList<>();

    /** Contains pre-resolved beans that are to be added to the context when it is built */
    private Set<ExternalBeanDefinition> externalBeanDefinitions = new HashSet<>();

    /**
     *
     */
    public XMLContextFactory() {
    }

    /**
     *
     * @return
     * @throws JAXBException
     */
    private final JAXBContext getJaxbContext()
            throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("org.nanocontext.xml");
        }
        return jaxbContext;
    }

    /**
     *
     * @param classLoader
     * @return
     */
    public XMLContextFactory withClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     *
     * @param beansType
     * @return
     */
    public XMLContextFactory with(final Beans beansType) {
        for (BeanType beanType : beansType.getBean()) {
            // default the scope to prototype
            if (beanType.getScope() == null) {
                beanType.setScope(ScopeType.PROTOTYPE);
            }

            with(beanType);
        }

        for (ResourceType artifactType : beansType.getArtifact()) {
            withArtifact(artifactType);
        }

        for (ResourceType propertiesType : beansType.getProperties()) {
            withProperty(propertiesType);
        }

        return this;
    }

    public XMLContextFactory with(final BeanType beanType) {
        if (beanType != null) {
            if (! this.beanTypes.contains(beanType)) {
                this.beanTypes.add(beanType);
            }
        }

        return this;
    }

    public XMLContextFactory withArtifact(final ResourceType artifact) {
        if (artifact != null) {
            this.artifacts.add(artifact);
        }

        return this;
    }

    public XMLContextFactory withProperty(final ResourceType propertiesType) {
        if (propertiesType != null) {
            this.properties.add(propertiesType);
        }

        return this;
    }


    /**
     *
     * @param identifier
     * @param bean
     * @return
     * @throws ContextInitializationException
     */
    public XMLContextFactory withExternalBeanDefinition(final String identifier, final Object bean)
            throws ContextInitializationException {
        if (bean != null) {
            withExternalBeanDefinition(new ExternalBeanDefinition(identifier, bean));
        }
        return this;
    }

    /**
     *
     * @param xBeanDef
     * @return
     */
    private XMLContextFactory withExternalBeanDefinition(ExternalBeanDefinition xBeanDef) {
        if (xBeanDef != null) {
            if (xBeanDef.getIdentifier() == null) {
                xBeanDef = new ExternalBeanDefinition(UUID.randomUUID().toString(), xBeanDef.beanInstance);
            }
            this.externalBeanDefinitions.add(xBeanDef);
        }

        return this;
    }

    /**
     * Set the context to delegate to when an ID cannot be found in the current
     * context.
     *
     * @param parent
     * @return
     * @throws ContextInitializationException
     */
    public XMLContextFactory withParentContext(final Context parent)
            throws ContextInitializationException {
        this.parent = parent;
        return this;
    }


    // ========================================================================================
    // Methods to read the context from an XML resource
    // ========================================================================================

    /**
     *
     * @param contextDefinition
     * @return
     * @throws JAXBException
     * @throws ContextInitializationException
     * @throws FileNotFoundException
     */
    public XMLContextFactory with(final File contextDefinition)
            throws JAXBException, ContextInitializationException, FileNotFoundException {
        FileInputStream fiS = new FileInputStream(contextDefinition);
        return with(fiS);
    }

    /**
     *
     * @param contextDefinition
     * @return
     * @throws JAXBException
     * @throws ContextInitializationException
     * @throws IOException
     */
    public XMLContextFactory with(final URL contextDefinition)
            throws JAXBException, ContextInitializationException, IOException {
        InputStream urlIS = contextDefinition.openStream();
        return with(urlIS);
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws JAXBException
     * @throws ContextInitializationException
     */
    public XMLContextFactory with(final InputStream inputStream)
            throws JAXBException, ContextInitializationException {
        JAXBContext jaxbContext = getJaxbContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Beans ctx = (Beans) unmarshaller.unmarshal(inputStream);
        return with(ctx);
    }

    // ========================================================================================
    // Build method and its helpers
    // ========================================================================================

    /**
     *
     * @return
     * @throws ContextInitializationException
     */
    public Context build()
            throws ContextInitializationException {
        Context ctx = new Context(this.parent);
        XMLPropertyFactory XMLPropertyFactory = new XMLPropertyFactory(ctx);

        Set<PropertiesHolder> propertiesHolders = extractPropertiesReferences(ctx);
        ctx.setProperties(propertiesHolders);

        Set<ArtifactHolder> artifactHolders = extractArtifactReferences(ctx);
        ctx.setArtifacts(artifactHolders);

        // create the top-level beansType (those directly under the 'beansType' element)
        Set<AbstractReferencableProperty> contextObjects = new HashSet<>();

        // add the externally defined bean definitions first
        if (this.externalBeanDefinitions != null) {
            for (ExternalBeanDefinition xBeanDef : this.externalBeanDefinitions) {
                AbstractReferencableProperty prop = XMLPropertyFactory.create(xBeanDef);
                contextObjects.add(prop);
            }
        }

        // for each of the top level beans, create an AbstractBeanInstanceFactory
        for (BeanType beanType : this.beanTypes) {
            // every bean has an identifier
            String id = beanType.getId();

            AbstractBeanInstanceFactory beanFactory = null;
            beanFactory = XMLPropertyFactory.createBeanInstanceFactory(beanType);
            contextObjects.add(beanFactory);
            contextObjects.addAll(createReferenced(beanFactory));
        }

        ctx.setContextObjectsMap(contextObjects);
        ctx.initialize();

        return ctx;
    }

    /**
     * A recursive map of references within the context.
     *
     * @param beanReference
     */
    private Set<AbstractReferencableProperty> createReferenced(final AbstractReferencableProperty<?> beanReference) {
        Set<AbstractReferencableProperty> result = new HashSet<>();

        if (beanReference instanceof AbstractReferencableProperty) {
            List<AbstractProperty> constructorParameters =
                    ((AbstractBeanInstanceFactory)beanReference).getConstructorParameterProperties();

            if (constructorParameters != null) {
                for (AbstractProperty constructorParameter : constructorParameters) {
                    if (constructorParameter instanceof AbstractReferencableProperty) {
                        result.add((AbstractReferencableProperty)constructorParameter);
                        result.addAll(
                                createReferenced((AbstractReferencableProperty) constructorParameter));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Extract the Artifact references in the XML elements into a Set of
     * ArtifactHolder instances.
     *
     * @throws InvalidArtifactSyntaxException
     */
    private Set<ArtifactHolder> extractArtifactReferences(final Context ctx) throws InvalidArtifactSyntaxException {
        Set<ArtifactHolder> artifactHolders = new HashSet<>(this.artifacts.size());

        // if no ClassLoader was specified then use the ClassLoader of this class
        ClassLoader classLoader = this.classLoader == null ?
                this.getClass().getClassLoader() :
                this.classLoader;

        // loop through each of the artifacts in the BeansType (root element)
        for (ResourceType artifactType : this.artifacts) {
            if (artifactType.getResource() != null) {
                try {
                    // an ArtifactHolder will load the Artifact
                    ArtifactHolder holder = new ArtifactHolder(artifactType.getId(), new URI(artifactType.getResource()), classLoader);
                    // note that the holder will not be added if it is a duplicate
                    artifactHolders.add(holder);
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new InvalidArtifactSyntaxException(artifactType, e);
                }
            }
        }

        return artifactHolders;
    }

    private Set<PropertiesHolder> extractPropertiesReferences(final Context ctx) throws InvalidPropertiesSyntaxException {
        Set<PropertiesHolder> holders = new HashSet<>(this.properties.size());

        // if no ClassLoader was specified then use the ClassLoader of this class
        ClassLoader classLoader = this.classLoader == null ?
                this.getClass().getClassLoader() :
                this.classLoader;

        // loop through each of the artifacts in the BeansType (root element)
        for (ResourceType propertiesType : this.properties) {
            if (propertiesType.getResource() != null) {
                try {
                    // an ArtifactHolder will load the Artifact
                    PropertiesHolder holder = new PropertiesHolder(ctx, propertiesType.getId(), new URI(propertiesType.getResource()));
                    // note that the holder will not be added if it is a duplicate
                    holders.add(holder);
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new InvalidPropertiesSyntaxException(propertiesType, e);
                }
            }
        }

        return holders;
    }

    /**
     * Where a Bean must be added to the context, while its lifecycle is outside of the control of the Context,
     * use this class as a holder and add it using withExternalBeanDefinition().
     */
    public static class ExternalBeanDefinition<T> {
        private final String identifier;
        private final T beanInstance;

        public ExternalBeanDefinition(final String identifier, final T beanInstance) {
            this.identifier = identifier;
            this.beanInstance = beanInstance;
        }

        public String getIdentifier() {
            return identifier;
        }

        public T getBeanInstance() {
            return beanInstance;
        }
    }

}
