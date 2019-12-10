//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.21 at 10:08:23 AM EDT 
//


package org.nanocontext.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.nanocontext.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Properties_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "properties");
    private final static QName _Artifact_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "artifact");
    private final static QName _Bean_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "bean");
    private final static QName _Value_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "value");
    private final static QName _List_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "list");
    private final static QName _Ref_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "ref");
    private final static QName _PropertiesRef_QNAME = new QName("http://nanocontext.org/v1/schema/ctx", "properties-ref");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.nanocontext.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link Beans }
     * 
     */
    public Beans createBeans() {
        return new Beans();
    }

    /**
     * Create an instance of {@link BeanType }
     * 
     */
    public BeanType createBeanType() {
        return new BeanType();
    }

    /**
     * Create an instance of {@link ListType }
     * 
     */
    public ListType createListType() {
        return new ListType();
    }

    /**
     * Create an instance of {@link ReferenceType }
     * 
     */
    public ReferenceType createReferenceType() {
        return new ReferenceType();
    }

    /**
     * Create an instance of {@link PropertiesReferenceType }
     * 
     */
    public PropertiesReferenceType createPropertiesReferenceType() {
        return new PropertiesReferenceType();
    }

    /**
     * Create an instance of {@link ConstructorArgType }
     * 
     */
    public ConstructorArgType createConstructorArgType() {
        return new ConstructorArgType();
    }

    /**
     * Create an instance of {@link ResourceType.Classpath }
     * 
     */
    public ResourceType.Classpath createResourceTypeClasspath() {
        return new ResourceType.Classpath();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "properties")
    public JAXBElement<ResourceType> createProperties(ResourceType value) {
        return new JAXBElement<ResourceType>(_Properties_QNAME, ResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "artifact")
    public JAXBElement<ResourceType> createArtifact(ResourceType value) {
        return new JAXBElement<ResourceType>(_Artifact_QNAME, ResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BeanType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "bean")
    public JAXBElement<BeanType> createBean(BeanType value) {
        return new JAXBElement<BeanType>(_Bean_QNAME, BeanType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "list")
    public JAXBElement<ListType> createList(ListType value) {
        return new JAXBElement<ListType>(_List_QNAME, ListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "ref")
    public JAXBElement<ReferenceType> createRef(ReferenceType value) {
        return new JAXBElement<ReferenceType>(_Ref_QNAME, ReferenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PropertiesReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nanocontext.org/v1/schema/ctx", name = "properties-ref")
    public JAXBElement<PropertiesReferenceType> createPropertiesRef(PropertiesReferenceType value) {
        return new JAXBElement<PropertiesReferenceType>(_PropertiesRef_QNAME, PropertiesReferenceType.class, null, value);
    }

}
