package org.nanocontext.maven;

import org.nanocontext.xml.BeanType;
import org.nanocontext.xml.ConstructorArgType;

import java.util.Comparator;

/**
 * A Comparator that orders by dependency between BeanType instances.
 * This can be used to order a Collection by dependents and dependencies, which
 * is important when instantiating objects.
 */
public class DependencyComparator implements Comparator<BeanType> {

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
        for (ConstructorArgType ctorArg : beanOne.getConstructorArg()) {
            if (ctorArg.getRef() != null) {
                if (beanTwoIdentifier.equals(ctorArg.getRef().getBean())) {
                    return 1;
                }
            }
        }

        // if the constructor args for bean one include a reference to bean two then
        // bean two must come before bean one (i.e. return 1)
        for (ConstructorArgType ctorArg : beanTwo.getConstructorArg()) {
            if (ctorArg.getRef() != null) {
                if (beanOneIdentifier.equals(ctorArg.getRef().getBean())) {
                    return -1;
                }
            }
        }

        return 0;
    }
}
