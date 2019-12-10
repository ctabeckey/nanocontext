package org.nanocontext.annotationsprocessor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.Locale;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class AnnotationsProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationsProcessorTest.class);

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][] {
                {"HelloWorld.java", null},
                {"IndependentBean.java", "org.nanocontext.application.NanoContextFactory"}
        };
    }

    @Test (dataProvider = "testDataProvider")
    public void test(final String sourceModuleName, final String expectedContextName) {
        File sourceModule = new File("./src/test/resources/" + sourceModuleName);
        Assert.assertTrue(sourceModule.exists());

        Compilation compilation = javac()
                .withProcessors(new AnnotationsProcessor())
                .withClasspathFrom(this.getClass().getClassLoader())
                .compile(JavaFileObjects.forResource(sourceModule.getName()));

        for (Diagnostic<? extends JavaFileObject> diagnostic : compilation.diagnostics()) {
            logger.info(diagnostic.toString());
        }

        if (expectedContextName != null)
            assertThat(compilation).generatedSourceFile(expectedContextName);

        assertThat(compilation).succeeded();
        for (JavaFileObject generatedFile : compilation.generatedFiles()) {
            logger.info(String.format("Generated %s (%s)", generatedFile.getName(), generatedFile.getKind().toString()));
        }
    }
}
