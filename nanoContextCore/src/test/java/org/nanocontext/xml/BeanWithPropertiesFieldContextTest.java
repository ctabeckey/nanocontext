package org.nanocontext.xml;

import org.nanocontext.core.Context;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.subjects.BeanWithPropertiesConstructorArg;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cbeckey on 4/7/17.
 */
public class BeanWithPropertiesFieldContextTest {
    private Context context = null;

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][] {
                new Object[]{"/OneBeanWithPropertiesFieldContext.org.nanocontext.xml",
                        "beanOne",
                        BeanWithPropertiesConstructorArg.class,
                        "getPropertyValue",
                        new String[][]{
                                new String[]{"hello", "world"},
                                new String[]{"black", "white"},
                                new String[]{"yes", "no"},
                        }
                },
        };
    }

    @Test(dataProvider = "testDataProvider")
    public void testBeanCreation(final String resourceName, final String identifier, final Class<?> beanClass, final String getterMethodName, final String[][] expectedKeyValuePairs)
            throws ContextInitializationException, JAXBException, InvocationTargetException, IllegalAccessException, InterruptedException, NoSuchMethodException {
        context = new XMLContextFactory()
                .with(this.getClass().getResourceAsStream(resourceName))
                .build();

        Object bean = context.getBean(identifier, beanClass);
        Assert.assertNotNull(bean);

        if (getterMethodName != null) {
            Method accessorMethod = beanClass.getMethod(getterMethodName, new Class<?>[]{String.class});
            if (expectedKeyValuePairs != null) {
                for (String[] expectedKeyValuePair : expectedKeyValuePairs) {
                    Object value = accessorMethod.invoke(bean, expectedKeyValuePair[0]);
                    Assert.assertEquals(value == null ? null : value.toString(), expectedKeyValuePair[1]);
                }
            }
        }

        context.shutdown();
    }

}
