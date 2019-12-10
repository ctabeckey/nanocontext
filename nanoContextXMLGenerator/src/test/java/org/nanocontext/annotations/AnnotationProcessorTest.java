package org.nanocontext.annotations;

import org.nanocontext.maven.AnnotationProcessor;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by cbeckey on 2/7/17.
 */
public class AnnotationProcessorTest {
    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> collector;
    private List<Processor> processors;

    @BeforeTest
    public void beforeTest() {
        collector = new DiagnosticCollector<>();
        compiler = ToolProvider.getSystemJavaCompiler();
        processors = Collections.singletonList(new AnnotationProcessor());

        fileManager = compiler.getStandardFileManager(collector, Locale.US, Charset.forName("UTF-8"));
    }

    @AfterTest
    public void afterTest() {
        System.out.println("Working directory is [" + System.getProperty("user.dir") + "]");
        for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
            System.out.println(diagnostic.toString());
        }

        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileManager = null;
        processors = null;
        compiler = null;
        collector = null;
    }

    @DataProvider
    public Object[][] sourceResourceDataProvider() {
        return new Object[][] {
                new Object[]{
                        "independentBeanContext.xml",
                        new File[]{new File("src/test/resources/IndependentBean.java")}
                },
                new Object[]{
                        "dependentBeanContext.xml",
                        new File[]{new File("src/test/resources/IndependentBean.java"),
                                new File("src/test/resources/DependentBean.java")}
                }
        };
    }

    //@Test(dataProvider = "sourceResourceDataProvider" )
    public void testCompilation(final String contextFileName, final File[] fileNames) {
        //streams.
        ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
        OutputStreamWriter stdout = new OutputStreamWriter(stdoutStream);

        JavaCompiler.CompilationTask task = compiler.getTask(
                stdout,
                fileManager,
                collector,Collections.singletonList("-Acontext=" + contextFileName),
                null,
                fileManager.getJavaFileObjects(fileNames)
        );
        task.setProcessors(processors);

        Boolean result = task.call();
        Assert.assertTrue(result.booleanValue());

        String stdoutS = new String(stdoutStream.toByteArray());
    }

    //@DataProvider
    public Object[][] annotationProcessorTestData() {
        return new Object[][] {
                new Object[]{"singleBeanContext.xml", new String[]{"TestSubjectTwo.java"}},
                new Object[]{"doubleBeanContext.xml", new String[]{"TestSubject.java", "TestSubjectTwo.java"}},
        };
    }

    //@Test(dataProvider = "annotationProcessorTestData")
    public void testAnnotationProcessor(final String outputContextFile, final String[] resourceNames) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Processor processor = new AnnotationProcessor();
        List<Processor> processors = Collections.singletonList(processor);

        DiagnosticListener<JavaFileObject> diagnostic = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                System.err.println(diagnostic.toString());
            }
        };

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostic,
                Locale.getDefault(),
                Charset.defaultCharset()
        );

        // prepare the source file(s) to compile
        List<File> sourceFileList = new ArrayList<File>();
        if (resourceNames != null) {
            for (String resourceName : resourceNames) {
                URL resourceLocation = this.getClass().getClassLoader().getResource(resourceName);
                Assert.assertNotNull(resourceLocation, String.format("Unable to find resource %s", resourceName));
                sourceFileList.add(new File(resourceLocation.getFile()));
            }

            List<String> options = Arrays.asList(String.format("-Acontext=%s", outputContextFile));

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostic, options, null, compilationUnits);
            task.setProcessors(processors);

            Boolean result = task.call();
            Assert.assertTrue(result.booleanValue());
        }

        try {fileManager.close();}
        catch (IOException e) {}
    }
}