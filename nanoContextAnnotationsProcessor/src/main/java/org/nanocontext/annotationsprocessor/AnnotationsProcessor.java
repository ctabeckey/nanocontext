package org.nanocontext.annotationsprocessor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.nanocontext.annotations.NanoBean;
import org.nanocontext.annotations.NanoInject;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 *
 */
public class AnnotationsProcessor implements Processor {
    private final static Set<String> supportedAnnotations;
    private final static Set<String> supportedOptions;
    public static final String CONTEXT_PACKAGE_OPTION = "package";
    public static final String CONTEXT_CLASSNAME_OPTION = "context";
    public static final String DEFAULT_CONTEXT_FACTORY_CLASSNAME = "NanoContextFactory";
    public static final String DEFAULT_CONTEXT_FACTORY_PACKAGE = "org.nanocontext.application";

    static {
        supportedAnnotations = new HashSet<>();

        // the list of annotations that this processor operates on
        supportedAnnotations.add(NanoBean.class.getCanonicalName());

        // NanoInject, NanoActivate and NanoProprtiesInject are annotations that are only
        // effective when they are inside a NanoBean annotated Class,
        // those annotations will be located within the NanoBean Class instances programmatically.

        // the options which the annotation processor understands
        supportedOptions = new HashSet<>();

        supportedOptions.add(CONTEXT_PACKAGE_OPTION);
        supportedOptions.add(CONTEXT_CLASSNAME_OPTION);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return supportedOptions;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    /**
     * Set when this annotation processor is initialized
     */
    private ProcessingEnvironment processingEnv;

    /**
     */
    private String contextPackageName;

    /**
     */
    private String contextClassName;

    private ContextFactoryGenerator contextFactoryGenerator;

    /**
     *
     * @param processingEnv - environment for facilities the tool framework provides to the processor
     */
    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;

        contextPackageName = this.processingEnv.getOptions().get(CONTEXT_PACKAGE_OPTION);
        if (contextPackageName == null || contextPackageName.length() == 0) {
            contextPackageName = DEFAULT_CONTEXT_FACTORY_PACKAGE;
        }

        contextClassName = this.processingEnv.getOptions().get(CONTEXT_CLASSNAME_OPTION);
        if (contextClassName == null || contextClassName.length() == 0) {
            contextClassName = DEFAULT_CONTEXT_FACTORY_CLASSNAME;
        }

        try {
            this.contextFactoryGenerator = new ContextFactoryGenerator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFullyQualifiedContextClassname() {
        return contextPackageName == null ? contextClassName : contextPackageName + "." + contextClassName;
    }

    /**
     * Processes a set of annotation types on type elements originating from the prior round and returns
     * whether or not these annotations are claimed by this processor. If true is returned, the annotations are
     * claimed and subsequent processors will not be asked to process them; if false is returned, the annotations
     * are unclaimed and subsequent processors may be asked to process them. A processor may always return the same
     * boolean value or may vary the result based on chosen criteria.
     *
     * @param annotations - the annotation types requested to be processed
     * @param roundEnv - environment for information about the current and prior round
     * @return - whether or not the set of annotations are claimed by this processor. always return
     *      false, we do not claim any annotations
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<BeanType> beans = new ArrayList<>();

        Set<? extends Element> nanoBeanElements = roundEnv.getElementsAnnotatedWith(NanoBean.class);
        if (nanoBeanElements != null) {
            for (Element element : nanoBeanElements) {
                TypeElement typeElement = (TypeElement) element;
                BeanType beanElement = generateXmlElement(typeElement, element.getAnnotation(NanoBean.class));

                ConstructorElementVisitor visitor = new ConstructorElementVisitor();
                List<ConstructorArgType> result = typeElement.accept(visitor, Collections.EMPTY_MAP);
                if (result != null) {
                    for (ConstructorArgType ctorArg : result) {
                        beanElement.getConstructorArgs().add(ctorArg);
                    }
                }

                beans.add(beanElement);
            }
        }

        if (beans.size() > 0) {
            ContextFactoryDataModel contextFactoryDataModel = new ContextFactoryDataModel();
            contextFactoryDataModel.setPackageName(contextPackageName);
            contextFactoryDataModel.setContextFactoryName(contextClassName);

            // sort the beans here so that the dependent beans come after their dependencies
            Collections.sort(beans, new DependencyComparator());
            contextFactoryDataModel.getBeans().addAll(beans);

            // write the configuration file to the generated resources
            JavaFileObject module = null;

            try {
                module = this.processingEnv.getFiler().createSourceFile(getFullyQualifiedContextClassname(), (Element[])null);

                try (OutputStreamWriter moduleWriter = new OutputStreamWriter(module.openOutputStream())) {
                    contextFactoryGenerator.writeContextFactory(contextFactoryDataModel, moduleWriter);
                } catch (IOException ioX) {
                    ioX.printStackTrace();
                } catch (TemplateException tX) {
                    tX.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     *
     * @param beans
     * @param moduleWriter
     */
    private void writeContextFactory(List<BeanType> beans, OutputStreamWriter moduleWriter)
            throws IOException, TemplateException {
        Configuration cfg = new Configuration();

        cfg.setDirectoryForTemplateLoading(new File("src/main/resources"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        Template template = cfg.getTemplate("ContextFactory.ftl");
        template.process(beans, moduleWriter);
    }

    /**
     * A Comparator that orders by dependency between BeanType instances.
     * This can be used to order a Collection by dependents and dependencies, which
     * is important when instantiating objects.
     */
    private class DependencyComparator implements Comparator<BeanType> {

        @Override
        public int compare(BeanType beanOne, BeanType beanTwo) {
            if(beanOne == null) {
                return 1;
            }
            if(beanTwo == null) {
                return -1;
            }

            final String beanOneIdentifier = beanOne.getId();
            final String beanTwoIdentifier = beanTwo.getId();

            // if the constructor args for bean one include a reference to bean two then
            // bean two must come before bean one (i.e. return 1)
            for (ConstructorArgType ctorArg : beanOne.getConstructorArgs()) {
                if (ctorArg.ref != null) {
                    if (beanTwoIdentifier.equals(ctorArg.ref.getBean())) {
                        return 1;
                    }
                }
            }

            // if the constructor args for bean one include a reference to bean two then
            // bean two must come before bean one (i.e. return 1)
            for (ConstructorArgType ctorArg : beanTwo.getConstructorArgs()) {
                if (ctorArg.ref != null) {
                    if (beanOneIdentifier.equals(ctorArg.ref.getBean())) {
                        return -1;
                    }
                }
            }

            return 0;
        }
    }

    /**
     *
     * R - the return type of this visitor's methods. Use Void for visitors that do not need to return results.
     * P - the type of the additional parameter to this visitor's methods.
     *     Use Void for visitors that do not need an additional parameter.
     */
    private class ConstructorElementVisitor extends SimpleElementVisitor8<List<ConstructorArgType>, Map<String, String>> {
        private List<ConstructorArgType> result = null;

        ConstructorElementVisitor() {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "ConstructorElementVisitor created."
            );
        }

        @Override
        protected List<ConstructorArgType> defaultAction(Element e, Map<String, String> options) {
            List<ConstructorArgType> result = super.defaultAction(e, options);
            if (result == null) {
                result = new ArrayList<>();
            }

            for (Element child : e.getEnclosedElements()) {
                result.addAll(child.accept(this, options));
            }

            return result;
        }

        /**
         *
         * @param executableElement
         * @param options
         * @return
         */
        @Override
        public List<ConstructorArgType> visitExecutable(ExecutableElement executableElement, Map<String, String> options) {
            List<ConstructorArgType> result = new ArrayList<>();
            final String elementName = executableElement.getSimpleName().toString();

            if (executableElement.getKind() == ElementKind.CONSTRUCTOR) {

                StringBuilder sb = new StringBuilder(elementName);
                sb.append('(');

                for (VariableElement parameter : executableElement.getParameters()) {
                    NanoInject injectAnnotation = parameter.getAnnotation(NanoInject.class);
                    if (injectAnnotation == null) {
                        result = null;
                        break;
                    }
                    String identifier = injectAnnotation.identifier();
                    TypeMirror parameterType = parameter.asType();
                    String parameterName = parameter.getSimpleName().toString();

                    ConstructorArgType ctorArg = new ConstructorArgType();
                    if (identifier == null) {
                        BeanType bean = new BeanType();
                        bean.setScope(ScopeType.PROTOTYPE);
                        bean.setClazz(parameterType.toString());
                        ctorArg.bean = bean;
                    } else {
                        ReferenceType ref = new ReferenceType();
                        ref.setBean(identifier);
                        ctorArg.ref = ref;
                    }
                    result.add(ctorArg);

                    sb.append(String.format("%s(identifier=%s) %s %s, ",
                        injectAnnotation.annotationType().getSimpleName(), identifier, parameterType.toString(), parameterName));
                }
                sb.append(')');

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        String.format("%s", sb.toString())
                );

            }

            this.result = result;
            return result;
        }
    }

    /**
     *
     * @param typeElement
     * @param nanoBeanAnnotation
     * @return
     */
    private BeanType generateXmlElement(TypeElement typeElement, NanoBean nanoBeanAnnotation) {
        String beanClassName = typeElement.getQualifiedName().toString();
        String beanIdentifier = nanoBeanAnnotation.identifier();
        boolean activeBean = nanoBeanAnnotation.active();
        boolean lazyLoadBean = nanoBeanAnnotation.lazyLoad();
        ScopeType beanScopeType = ScopeType.fromValue(nanoBeanAnnotation.scope());
        String activateMethod = nanoBeanAnnotation.activate();

        return generateXmlElement(
                beanClassName,
                beanIdentifier,
                beanScopeType,
                activeBean,
                activeBean ? activateMethod : null,
                lazyLoadBean);
    }

    /**
     *
     * @param beanClassName
     * @param identifier
     * @param scopeType
     * @return
     */
    private BeanType generateXmlElement(
            final String beanClassName, final String identifier, final ScopeType scopeType,
            final boolean activeBean,
            final String activateMethodName,
            final boolean lazyLoadBean) {
        BeanType beanType = new BeanType();
        beanType.setClazz(beanClassName);
        beanType.setScope(scopeType);
        if (activeBean) {
            beanType.setActive(true);
            beanType.setActivateMethod(activateMethodName);
        }
        beanType.setLazyLoad(Boolean.valueOf(lazyLoadBean));

        if (identifier != null && identifier.length() > 0) {
            beanType.setId(identifier);
        }

        return beanType;
    }

    /**
     *
     * @param element
     * @param annotation
     * @param member
     * @param userText
     * @return - always return null, this class does not provide Completions
     */
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return null;
    }
}
