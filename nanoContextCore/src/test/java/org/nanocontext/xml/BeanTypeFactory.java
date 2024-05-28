package org.nanocontext.xml;

import org.nanocontext.xml.BeanType;
import org.nanocontext.xml.ConstructorArgType;
import org.nanocontext.xml.ScopeType;

import java.util.List;

/**
 * Created by cbeckey on 1/23/17.
 */
public class BeanTypeFactory {
    public static BeanType create (
            String id, String clazz, ScopeType scope, String artifact,
            String factory, String factoryClass, String factoryMethod,
            Boolean active,
            List<ConstructorArgType> constructorArg) {
        BeanType beanType = new BeanType();

        beanType.setId(id);
        beanType.setClazz(clazz);
        beanType.setScope(scope);
        beanType.setArtifact(artifact);
        beanType.setFactory(factory);
        beanType.setFactoryClass(factoryClass);
        beanType.setFactoryMethod(factoryMethod);
        beanType.setActive(active);

        if (constructorArg != null) {
            beanType.getConstructorArg().addAll(constructorArg);
        }

        return beanType;
    }


}
