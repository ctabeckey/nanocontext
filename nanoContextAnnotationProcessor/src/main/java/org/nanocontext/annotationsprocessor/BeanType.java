package org.nanocontext.annotationsprocessor;

import java.util.HashMap;
import java.util.List;

/**
 *
 */
class BeanType extends HashMap<String, Object> {
    public final static String TEMPLATE_CONSTRUCTOR_ARGS = "constructorArgs";
    public final static String TEMPLATE_ID = "id";
    public final static String TEMPLATE_CLAZZ = "clazz";
    public final static String TEMPLATE_SCOPE = "scope";
    public final static String TEMPLATE_ARTIFACT = "artifact";
    public final static String TEMPLATE_LAZY_LOAD = "lazyLoad";
    public final static String TEMPLATE_FACTORY = "factory";
    public final static String TEMPLATE_FACTORY_CLASS = "factoryClass";
    public final static String TEMPLATE_FACTORY_METHOD = "factoryMethod";
    public final static String TEMPLATE_ACTIVE = "active";
    public final static String TEMPLATE_ACTIVATE_METHOD = "activateMethod";
    public final static String TEMPLATE_INITIALIZE_METHOD = "initializeMethod";
    public final static String TEMPLATE_FINALIZE_METHOD = "finalizeMethod";

    public List<ConstructorArgType> getConstructorArgs() {
        return (List<ConstructorArgType>) this.get(TEMPLATE_CONSTRUCTOR_ARGS);
    }

    void setConstructorArgs(List<ConstructorArgType> constructorArgs) {
        this.put(TEMPLATE_CONSTRUCTOR_ARGS, constructorArgs);
    }

    public String getId() {
        return (String) this.get(TEMPLATE_ID);
    }

    void setId(String id) {
        this.put(TEMPLATE_ID, id);
    }

    public String getClazz() {
        return (String) this.get(TEMPLATE_CLAZZ);
    }

    void setClazz(String clazz) {
        this.put(TEMPLATE_CLAZZ, clazz);
    }

    public ScopeType getScope() {
        String scopeString = (String)this.get(TEMPLATE_SCOPE);
        return scopeString == null ? null : ScopeType.fromValue(scopeString);
    }

    void setScope(ScopeType scope) {
        this.put(TEMPLATE_SCOPE, scope.value());
    }

    public String getArtifact() {
        return (String) this.get(TEMPLATE_ARTIFACT);
    }

    void setArtifact(String artifact) {
        this.put(TEMPLATE_ARTIFACT, artifact);
    }

    public Boolean getLazyLoad() {
        return (Boolean) this.get(TEMPLATE_LAZY_LOAD);
    }

    void setLazyLoad(final Boolean lazyLoad) {
        this.put(TEMPLATE_LAZY_LOAD, lazyLoad);
    }

    public String getFactory() {
        return (String) this.get(TEMPLATE_FACTORY);
    }

    void setFactory(String factory) {
        this.put(TEMPLATE_FACTORY, factory);
    }

    public String getFactoryClass() {
        return (String) this.get(TEMPLATE_FACTORY_CLASS);
    }

    void setFactoryClass(String factoryClass) {
        this.put(TEMPLATE_FACTORY_CLASS, factoryClass);
    }

    public String getFactoryMethod() {
        return (String) this.get(TEMPLATE_FACTORY_METHOD);
    }

    void setFactoryMethod(String factoryMethod) {
        this.put(TEMPLATE_FACTORY_METHOD, factoryMethod);
    }

    public Boolean getActive() {
        return (Boolean)this.get(TEMPLATE_ACTIVE);
    }

    void setActive(Boolean active) {
        this.put(TEMPLATE_ACTIVE, active);
    }

    public String getActivateMethod() {
        return (String) this.get(TEMPLATE_ACTIVATE_METHOD);
    }

    void setActivateMethod(String activateMethod) {
        this.put(TEMPLATE_ACTIVATE_METHOD, activateMethod);
    }

    public String getInitializeMethod() {
        return (String) this.get(TEMPLATE_INITIALIZE_METHOD);
    }

    void setInitializeMethod(String initializeMethod) {
        this.put(TEMPLATE_INITIALIZE_METHOD, initializeMethod);
    }

    public String getFinalizeMethod() {
        return (String) this.get(TEMPLATE_FINALIZE_METHOD);
    }

    void setFinalizeMethod(String finalizeMethod) {
        this.put(TEMPLATE_FINALIZE_METHOD, finalizeMethod);
    }
}
