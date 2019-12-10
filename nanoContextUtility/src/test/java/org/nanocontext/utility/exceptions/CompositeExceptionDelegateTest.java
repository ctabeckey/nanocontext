package org.nanocontext.utility.exceptions;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Collections;

/**
 * Tests for the CompositeExceptionDelegate helper
 */
@Test(singleThreaded = true)
public class CompositeExceptionDelegateTest {
    /**
     * Test an empty instance of CompositeExceptionDelegate.
     * It should not throw exceptions but may return empty values
     */
    @Test
    public void testDegenerateInstance() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate();
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.getMessage());
    }

    /**
     * Test an empty instance of CompositeExceptionDelegate.
     * It should not throw exceptions but may return empty values
     */
    @Test
    public void testSingleElementInstance() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(new Exception("Hello World"));
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.getMessage());
        Assert.assertEquals(x.getMessage(), "Hello World");
    }

    /**
     * Test the constructor that takes a collection
     */
    @Test
    public void testCollectionConstructor() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(
                Collections.singleton(new Exception("Hello World"))
        );
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.getMessage());
        Assert.assertEquals(x.getMessage(), "Hello World");
    }

    /**
     * Tes the localized message creation
     */
    @Test
    public void testSingleElementInstanceLocalizedMessage() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(new Exception("Hello World"));
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.getLocalizedMessage());
        Assert.assertEquals(x.getLocalizedMessage(), "Hello World");
    }

    /**
     * Test adding an exception to an existing instance
     */
    @Test
    public void testAddingSingleElementInstance() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(new Exception("Hello World"));
        x.addException(new Exception("Hello World"));
        Assert.assertNotNull(x.getMessage());
        Assert.assertEquals(x.getMessage(),
                "Hello World\n" +
                "Hello World"
        );
    }

    /**
     * test multiple exceptions in the constructor
     */
    @Test
    public void testMultipleElementInstance() {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate<>(new Exception[]{
                new Exception("Hello World"),
                new Exception("Hello World"),
                new Exception("Hello World")
        });

        Assert.assertNotNull(x.getMessage());
        Assert.assertEquals(x.getMessage(),
                "Hello World\n" +
                "Hello World\n" +
                "Hello World"
        );
    }

    /**
     * test adding new elements to an existing instance with multiple exceptions
     */
    @Test
    public void testElementsAddedToInstance() {

        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate<>(new Exception[]{
                new Exception("Hello World"),
                new Exception("Hello World"),
                new Exception("Hello World")
        });

        x.addException(new Exception("Hello World"));

        Assert.assertNotNull(x.getMessage());
        Assert.assertEquals(x.getMessage(),
                "Hello World\n" +
                "Hello World\n" +
                "Hello World\n" +
                "Hello World"
        );
    }

    /**
     * test printing stack trace to System.err
     * @throws IOException
     */
    @Test
    public void testPrintStackTrace() throws IOException {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(new Exception("Hello World"));
        Assert.assertNotNull(x);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048);
        PrintStream destination = new PrintStream(outStream);

        PrintStream err = System.err;
        System.setErr(destination);
        x.printStackTrace();
        System.err.flush();
        System.setErr(err);

        assertMethodNameInStackTrace(
                outStream,
                "org.nanocontext.utility.exceptions.CompositeExceptionDelegateTest.testPrintStackTrace"
        );
    }

    /**
     * test printing the stack trace to a given stream
     */
    @Test
    public void testPrintStackTraceOutStream() throws IOException {
        CompositeExceptionDelegate<Exception> x = new CompositeExceptionDelegate(new Exception("Hello World"));
        Assert.assertNotNull(x);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(2048);
        PrintStream destination = new PrintStream(outStream);
        x.printStackTrace(destination);

        assertMethodNameInStackTrace(
                outStream,
                "org.nanocontext.utility.exceptions.CompositeExceptionDelegateTest.testPrintStackTraceOutStream"
        );
    }

    /**
     * helper method to (marginally) validate a stack trace
     * @param outStream
     * @param methodName
     * @throws IOException
     */
    private void assertMethodNameInStackTrace(final ByteArrayOutputStream outStream, String methodName) throws IOException {
        byte[] output = outStream.toByteArray();
        Assert.assertTrue(output.length > 0);

        ByteArrayInputStream inStream = new ByteArrayInputStream(output);
        InputStreamReader inReader = new InputStreamReader(inStream);
        char[] buffy = new char[256];
        StringBuilder sb = new StringBuilder();
        for (int byteRead = inReader.read(buffy); byteRead >= 0; byteRead = inReader.read(buffy)) {
            sb.append(buffy);
        }
        String stackTraceOutput = sb.toString();

        // This method should be at the top of the stack trace.
        // otherwise this just has to look like a stack trace
        Assert.assertTrue(stackTraceOutput.contains(methodName));
    }

}
