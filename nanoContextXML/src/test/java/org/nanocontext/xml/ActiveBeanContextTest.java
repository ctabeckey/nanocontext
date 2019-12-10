package org.nanocontext.xml;

import org.nanocontext.core.Context;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.nanocontext.xml.subjects.SimpleActiveBean;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cbeckey on 3/13/17.
 */
public class ActiveBeanContextTest {
    private Context context = null;

    @DataProvider
    public Object[][] activeBeanTestDataProviderTest() {
        return new Object[][] {
                new Object[]{"/OneActiveBeanContext.xml", "beanOne", SimpleActiveBean.class, "getValue", SimpleActiveBean.ranValue}
        };
    }

    @Test(dataProvider = "activeBeanTestDataProviderTest")
    public void testActiveBeanCreation(final String resourceName, final String identifier, final Class<?> beanClass, final String accessorMethodName, final Object expectedValue)
            throws ContextInitializationException, JAXBException, InvocationTargetException, IllegalAccessException, InterruptedException, NoSuchMethodException {
        context = new XMLContextFactory()
                .with(this.getClass().getResourceAsStream(resourceName))
                .build();

        // give the active bean time to start
        Thread.currentThread().sleep(20000L);

        Object bean = context.getBean(identifier, beanClass);
        Assert.assertNotNull(bean);

        if (accessorMethodName != null) {
            Method accessorMethod = bean.getClass().getMethod(accessorMethodName, (Class<?>[]) null);
            Object actualValue = accessorMethod.invoke(bean);
            Assert.assertEquals(actualValue, expectedValue);
        }

        context.shutdown();
    }

}
