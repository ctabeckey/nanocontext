package org.nanocontext.xml;

import org.nanocontext.core.AbstractBeanInstanceFactory;
import org.nanocontext.core.AbstractProperty;
import org.nanocontext.core.Context;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.subjects.BeanWithStaticConstructor;
import org.nanocontext.xml.subjects.GenericBeanFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by cbeckey on 3/14/16.
 */
public class AbstractBeanInstanceFactoryTest {
    private XMLContextFactory ctxFactory;
    private Context ctx;

    @BeforeTest
    public void beforeTest() throws ContextInitializationException {
        ClassMatcher classMatcher = new ClassMatcher(BeanWithStaticConstructor.class);

        ctxFactory = new XMLContextFactory();

        BeanType bean = new BeanType();
        bean.setId("id1");
        bean.setArtifact(null);
        bean.setClazz(BeanWithStaticConstructor.class.getName());
        bean.setScope(ScopeType.PROTOTYPE);
        ctxFactory.with(bean);

        bean = new BeanType();
        bean.setId("id2");
        bean.setArtifact(null);
        bean.setClazz(BeanWithStaticConstructor.class.getName());
        bean.setScope(ScopeType.PROTOTYPE);
        ctxFactory.with(bean);

        bean = new BeanType();
        bean.setId("genericBeanFactory");
        bean.setArtifact(null);
        bean.setClazz(GenericBeanFactory.class.getName());
        bean.setScope(ScopeType.SINGLETON);
        ctxFactory.with(bean);

        ctx = ctxFactory
                .build();
    }

    @Test
    public void testProperties() throws ContextInitializationException {
        BeanType beanType = new BeanType();
        beanType.setId("id");
        beanType.setArtifact(null);
        beanType.setClazz(BeanWithStaticConstructor.class.getName());
        beanType.setScope(ScopeType.PROTOTYPE);

        AbstractBeanInstanceFactory ref = new MockAbstractBeanInstanceFactory(ctx, beanType);

        Assert.assertEquals(ref.getIdentifier(), "id");
        Assert.assertEquals(ref.getClazzName(), BeanWithStaticConstructor.class.getName());
        Assert.assertEquals(ref.getContext(), ctx);
    }

    @Test
    public void testNegativeCircularDetection() throws ContextInitializationException {
        BeanType beanType = new BeanType();
        beanType.setId("id");
        beanType.setArtifact(null);
        beanType.setClazz(BeanWithStaticConstructor.class.getName());
        beanType.setScope(ScopeType.PROTOTYPE);

        MockAbstractBeanInstanceFactory ref = new MockAbstractBeanInstanceFactory(ctx, beanType);
    }

    @DataProvider(name = "validBeanDataProvider")
    public Object[][] validBeanDataProvider() {
        return new Object[][] {
                new Object[]{ctx,
                        BeanTypeFactory.create("id", BeanWithStaticConstructor.class.getName(), ScopeType.PROTOTYPE, null,
                                null, null, null,
                                Boolean.FALSE,
                                null)
                },
                new Object[]{ctx,
                        BeanTypeFactory.create("id", BeanWithStaticConstructor.class.getName(), ScopeType.PROTOTYPE, null,
                                null, null, "createBean",
                                Boolean.FALSE,
                                null)
                },
                new Object[]{ctx,
                        BeanTypeFactory.create("id", BeanWithStaticConstructor.class.getName(), ScopeType.PROTOTYPE, null,
                                null, BeanWithStaticConstructor.class.getName(), "createBean",
                                Boolean.FALSE,
                                null)
                },
                new Object[]{ctx,
                        BeanTypeFactory.create("id", BeanWithStaticConstructor.class.getName(), ScopeType.PROTOTYPE, null,
                                null, GenericBeanFactory.class.getName(), "createBean",
                                Boolean.FALSE,
                                null)
                },
                new Object[]{ctx,
                        BeanTypeFactory.create("id", BeanWithStaticConstructor.class.getName(), ScopeType.PROTOTYPE, null,
                                "genericBeanFactory", null, "createBeanInstance",
                                Boolean.FALSE,
                                null)
                }
        };
    }

    @Test(dataProvider = "validBeanDataProvider")
    public void testValidClassName(final Context ctx, final BeanType beanType) throws ContextInitializationException {
        AbstractBeanInstanceFactory ref = new MockAbstractBeanInstanceFactory(ctx, beanType);

        Assert.assertNotNull(ref);
        Assert.assertNotNull(ref.createBeanInstance());
    }

    @DataProvider(name = "invalidBeanDataProvider")
    public Object[][] invalidBeanDataProvider() {
        return new Object[][] {
                new Object[]{BeanTypeFactory.create("id", "org.nanocontext.invalid.ClazzName", ScopeType.PROTOTYPE, null,
                                null, null, null,
                                Boolean.FALSE,
                                null)
                }
        };
    }

    @Test(dataProvider = "invalidBeanDataProvider", expectedExceptions = {ContextInitializationException.class})
    public void testInvalidClassName(final BeanType beanType) throws ContextInitializationException {
        AbstractBeanInstanceFactory ref = new MockAbstractBeanInstanceFactory(ctx, beanType);

        ref.createBeanInstance();
    }

    /**
     *
     */
    private static class MockAbstractBeanInstanceFactory
            extends AbstractBeanInstanceFactory {

        public MockAbstractBeanInstanceFactory(Context context, BeanType beanType) throws ContextInitializationException {
            super(context,
                    beanType.getId(), beanType.getArtifact(), beanType.getClazz(),
                    beanType.getFactory(), beanType.getFactoryClass(), beanType.getFactoryMethod(),
                    beanType.getLazyLoad(),
                    beanType.getActive(), beanType.getActivateMethod(),
                    beanType.getInitializeMethod(), beanType.getFinalizeMethod(),
                    (List<AbstractProperty>)null
            );
        }

        @Override
        public Context getContext() {
            return super.getContext();
        }

        /**
         * Get the value as the currently resolved type
         */
        @Override
        public Object getValue() throws ContextInitializationException {
            return createBeanInstance();
        }

        /**
         * Only works if the target class is a superclass result of a getValueType() call.
         *
         * @param targetClazz the target type
         * @return a cast of the getPropertyValue() result
         * @throws ContextInitializationException - if the target type is not the type or a super-type
         */
        @Override
        public Object getValue(Class targetClazz) throws ContextInitializationException {
            return super.getValue(targetClazz);
        }
    }

}
