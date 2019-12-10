package org.nanocontext.annotationsprocessor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class ContextFactoryGeneratorTest {
    private static Logger logger = LoggerFactory.getLogger(ContextFactoryGeneratorTest.class);

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][] {
                {new ContextFactoryDataModel()}
        };
    }

    @Test(dataProvider="testDataProvider")
    public void testGeneration(final ContextFactoryDataModel dataModel) throws IOException, TemplateException {
        Context ctx = new Context();

        ByteArrayOutputStream destination = new ByteArrayOutputStream(4096);
        try (OutputStreamWriter moduleWriter = new OutputStreamWriter(destination)) {
            ctx.getCtxFactoryGenerator().writeContextFactory(dataModel, moduleWriter);
        }

        ByteArrayInputStream inStream = new ByteArrayInputStream(destination.toByteArray());
        char[] buffy = new char[2048];
        try (InputStreamReader reader = new InputStreamReader(inStream)) {
            StringBuilder sb = new StringBuilder();
            for (int charsRead = reader.read(buffy); charsRead >= 0; charsRead = reader.read(buffy))
                sb.append(new String(buffy, 0, charsRead));

            String source = sb.toString();
            logger.info(source);

            Compilation compilation = ctx.getCompiler()
                    .compile(JavaFileObjects.forSourceString(dataModel.getContextFactoryName(), source));

            assertThat(compilation).succeeded();
        }
    }

    public static class Context {
        final private ContextFactoryGenerator ctxFactoryGenerator;
        final private com.google.testing.compile.Compiler compiler;

        public Context() throws IOException {
            ctxFactoryGenerator = new ContextFactoryGenerator();

            compiler = javac()
                    .withClasspathFrom(this.getClass().getClassLoader());
        }

        public ContextFactoryGenerator getCtxFactoryGenerator() {
            return ctxFactoryGenerator;
        }

        public Compiler getCompiler() {
            return compiler;
        }
    }
}
