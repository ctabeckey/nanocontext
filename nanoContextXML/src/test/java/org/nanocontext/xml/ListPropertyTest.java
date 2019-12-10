package org.nanocontext.xml;

import org.nanocontext.core.Context;
import org.nanocontext.core.ListProperty;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.utility.exceptions.UnknownCollectionTypeException;
import org.nanocontext.xml.subjects.BeanWithStaticConstructor;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cbeckey on 3/16/16.
 */
public class ListPropertyTest {
    private Context emptyCtx;
    private XMLPropertyFactory XMLPropertyFactory;

    @BeforeTest
    public void beforeTest() throws ContextInitializationException {
        emptyCtx = new XMLContextFactory().build();
        XMLPropertyFactory = new XMLPropertyFactory(emptyCtx);
    }

    @DataProvider
    public Object[][] isApplicableAsArrayPositiveDataProvider() {
        BeanType beanType1 = new BeanType();
        beanType1.setId("id1");
        beanType1.setScope(ScopeType.PROTOTYPE);
        beanType1.setClazz(BeanWithStaticConstructor.class.getName());
        beanType1.setArtifact(null);

        BeanType beanType2 = new BeanType();
        beanType2.setId("id2");
        beanType2.setScope(ScopeType.PROTOTYPE);
        beanType2.setClazz(BeanWithStaticConstructor.class.getName());
        beanType2.setArtifact(null);

        ReferenceType referenceType1 = new ReferenceType();
        referenceType1.setBean("id1");

        return new Object[][] {
                // simple String and primitive list elements
                new Object[]{
                        String.class,
                        Arrays.asList("Hello", "World")
                },
                new Object[]{
                        Integer.class,
                        Arrays.asList("1", "2")
                },
                new Object[]{
                        Float.class,
                        Arrays.asList("1", "42.24")
                },
                // more complex, business types
                new Object[]{
                        BeanWithStaticConstructor.class,
                        Arrays.asList(beanType1)
                },
                new Object[]{
                        BeanWithStaticConstructor.class,
                        Arrays.asList(beanType1, beanType2)
                },
                new Object[]{
                        Object.class,
                        Arrays.asList(beanType1, beanType2)
                },
        };
    }

    @Test(dataProvider = "isApplicableAsArrayPositiveDataProvider")
    public void testPositiveIsApplicableAsArray(final Class<?> componentClazz, final List<Object> beanOrValueOrList)
            throws ContextInitializationException {
        testIsApplicableAsArray(componentClazz, beanOrValueOrList);
    }

    @DataProvider
    public Object[][] isApplicableAsArrayNegativeDataProvider() {
        BeanType beanType1 = new BeanType();
        beanType1.setId("id1");
        beanType1.setScope(ScopeType.PROTOTYPE);
        beanType1.setClazz(BeanWithStaticConstructor.class.getName());
        beanType1.setArtifact(null);

        BeanType beanType2 = new BeanType();
        beanType2.setId("id2");
        beanType2.setScope(ScopeType.PROTOTYPE);
        beanType2.setClazz(BeanWithStaticConstructor.class.getName());
        beanType2.setArtifact(null);

        ReferenceType referenceType1 = new ReferenceType();
        referenceType1.setBean("id1");

        return new Object[][] {
                new Object[]{
                        String.class,
                        Arrays.asList("Hello", beanType1)
                },
                new Object[]{
                        Float.class,
                        Arrays.asList("1", "World")
                },
        };
    }

    @Test(dataProvider = "isApplicableAsArrayNegativeDataProvider", expectedExceptions = {UnknownCollectionTypeException.class})
    public void testNegativeIsApplicableAsArray(final Class<?> componentClazz, final List<Object> beanOrValueOrList)
            throws ContextInitializationException {
        testIsApplicableAsArray(componentClazz, beanOrValueOrList);
    }

    private void testIsApplicableAsArray(final Class<?> componentClazz, final List<Object> beanOrValueOrList)
            throws ContextInitializationException {
        ObjectFactory of = new ObjectFactory();
        ListType listType = of.createListType();
        listType.getBeanOrValueOrList().addAll(beanOrValueOrList);

        ListProperty list = XMLPropertyFactory.createList(listType);
        list.morph(List.class, componentClazz).getValue();
    }

}
