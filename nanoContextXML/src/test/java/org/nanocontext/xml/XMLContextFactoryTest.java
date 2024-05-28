package org.nanocontext.xml;

import org.nanocontext.core.Context;
import org.nanocontext.core.exceptions.ContextInitializationException;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;

public class XMLContextFactoryTest {
    @Test
    void buildSimple() throws JAXBException, FileNotFoundException, ContextInitializationException {
        File contextSource = new File("OneActiveBeanContext.xml");

        XMLContextFactory xmlContextFactory = new XMLContextFactory();
        xmlContextFactory.with(contextSource);
        Context ctx = xmlContextFactory.build();
    }
}
