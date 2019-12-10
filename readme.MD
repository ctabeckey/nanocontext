# Introduction
nanoContext is, as the name suggest, a very small framework to build application context. 
nanoContext is something like Spring core though with a reduced feature set and executable size.
The nanoContext jar, with its dependencies included, is currently about 100K and the intent is to keep it there.
nanoContext is a general purpose dependency injection framework built to excel in a cloud environment with something like Amazon Lambda functions.
nanoContext targets configurable, short lived, applications that may be instantiated on demand while meeting reasonable service level agreements. 
For that reason the emphasis is on small deployment size (to reduce download time from a repository), fast startup and remote resource accessibility.

## Configuration
nanoContext uses an XML configuration file that echos Spring configuration:
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
    <bean id="beanOne" class="ConstructorTestSubject" scope="singleton">
    </bean>
</beans>
```
nanoContext supports constructor arguments for dependency injection, it does not support property injection. 
This was a conscious decision for both executable size and performance, and because instances are thereby valid once constructed.
Constructor arguments may be bean declarations, references to existing beans, constants, lists or property maps.
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
     <bean id="beanOne" class="ConstructorTestSubject" scope="singleton">
         <constructor-arg>
             <value>hello</value>
         </constructor-arg>
     </bean>
 </beans>
```
```java
package org.nanocontext.core.subjects;

public class ConstructorTestSubject {
    public ConstructorTestSubject(final String lue) {
        ...
    }
}
```
 
nanoContext supports both static and bean factory methods. 
When using a factory method or factory class the parameters to the factory method are still specified using constructor-arg elements.
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
     <bean id="beanOne" class="ConstructorTestSubject" scope="singleton" factory-method="create">
         <constructor-arg>
             <value>hello</value>
         </constructor-arg>
     </bean>
 </beans>
```
```java
package org.nanocontext.core.subjects;

public class ConstructorTestSubject {
    public static ConstructorTestSubject create(final int lue) {
        ...
    }
    private ConstructorTestSubject() {
        ...
    }
}
```

nanoContext can download and instantiate beans from remote resources
```xml
    <artifact id="commonsio">
        <resource>https://mavencentral.com/nexus/service/local/repositories/central/content/commons-io/commons-io/2.4/commons-io-2.4.jar</resource>
    </artifact>
    <bean id="nullInStream"
          class="org.apache.commons.io.input.NullInputStream"
          scope="singleton"
          artifact="commonsio">
        <constructor-arg>
            <value>1024</value>
        </constructor-arg>
    </bean>
```
nanoContext implements "active" classes, which are simply executable classes. Active classes are started at framework startup time. 
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
    <bean id="beanOne" class="SimpleActiveBean" scope="singleton" active="true">
    </bean>
</beans>
```
```java
package org.nanocontext.core.subjects;

public class SimpleActiveBean implements Runnable {
    public void run() {
        ...
    }
}
```
An active class may either implement the java.lang.Runnable interface or not, in which case the name of the method to start the instance must be provided.
The method must take no arguments.
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
    <bean id="beanOne" class="org.nanocontext.core.subjects.AnotherActiveBean" scope="singleton" active="true" activate-method="doSomething">
    </bean>
</beans>
```
```java
package org.nanocontext.core.subjects;

public class SimpleActiveBean {
    public void doSomething() {
        ...
    }
}
```

The schema definition for the context XML may be found in the nanoXmlBeans project in src/main/resources/ctx.xsd.

## Programming
Creating a context is done programmatically:

```java
Context ctx = new ContextFactory()
                 .with(getClass()
                 .getClassLoader().getResourceAsStream("context.xml"))
                 .build();
```

after a context is created, beans references may be obtained from the context with the identifier and type:

```java
Account beanInstance = currentContext.getBean("bean-identifier", org.nanocontext.example.Account.class);
```
or simply by type where there is no possibility for confusion

```java
Account beanInstance = currentContext.getBean(org.nanocontext.example.Account.class);
```
nanoContext implements code to select the most specific type that satisfies a request for a bean. 
Similarly, the constructors and factory methods are selected to be most specific to the types given.
Nonetheless there are conditions under which the specificity of the constructor or factory methods is not sufficient
to determine programmer intent. In this case, nanoContext will select an available implementation without comment.

nanoContext supports hierarchical contexts:

```java
ContextFactory ctxFactory = new ContextFactory()
                      .with(getClass().getClassLoader()
                      .getResourceAsStream(resourceName));
  ctxFactory.withParentContext(parentContext);
  context = ctxFactory.build();
```

Context hierarchy is strictly one-way, calls to access a bean in a child that may not be satisfied by the child will be delegated to the parent.
Sibling contexts have no visibility to one another, nor do parent contexts have any visibility to their child contexts.

nanoContext does not resolve forward (or circular) references, the bean declarations must be in order.

## Annotations
nanoContext does not implement runtime classpath scanning like Spring. This was another conscious decision to reduce both startup time and deployed size.
nanoAnnotations and nanoAnnotationProcessor implement a compile-time annotation configuration. 
nanoAnnotationProcessor generates a configuration file from annotations.
Compiling the following class files
```java
@NanoBean(identifier = "independent-bean")
public class IndependentBean {
    public IndependentBean() {}
}

@NanoBean(identifier = "dependent-bean")
public class DependentBean {
    public DependentBean(@nanoInject(identifier = "independentBean") IndependentBean bean) {}
}
```
generates a context configuration like this
```xml
<beans xmlns="http://core.nanocontext.org/v1/schema/ctx">
    <bean id="independent-bean" class="IndependentBean" scope="singleton" lazy-load="false"/>
    <bean id="dependent-bean" class="DependentBean" scope="singleton" lazy-load="false">
        <constructor-arg>
            <ref bean="independent-bean"/>       
        </constructor-arg>
    </bean>
</beans>
```

## Deployment
The modules within nanoContextParent are:
* nanoXmlBeans - defines the configuration XSD and include he JAXB generated classes bound to that.
* nanoContext - the context implementation itself.
* nanoAnnotations - annotation declarations for compile-time configuration file generation.
* nanoAnnotationProcessor - the annotation processor that generates the configuration file from annotations.

## Contributing
Contributions to code which are consistent with the philosophy of minimal size and fast startup are encouraged. 
Contributions which significantly increase deployed size or slow startup should be implemented as dependent, wrapper,
implementations.