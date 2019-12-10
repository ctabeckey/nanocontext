package org.nanocontext.utility.rsc;

import org.nanocontext.utility.url.URLFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Test the RSC protocol handler
 */
public class HandlerTest {
    /**
     * Test that the protocol handler can open a resource and get the content
     */
    @Test
    public void testOpenConnection()
    throws IOException {
        Handler handler = new Handler();
        Assert.assertNotNull(handler);

        URLConnection conn = handler.openConnection(URLFactory.create("rsc:junk.txt"));
        Assert.assertNotNull(conn);

        InputStream in = conn.getInputStream();
        Assert.assertNotNull(in);

        InputStreamReader reader = new InputStreamReader(in);
        Assert.assertNotNull(reader);

        char[] buffy = new char[2048];      // just a big number
        reader.read(buffy);

        String content = new String(buffy);
        Assert.assertNotNull(content);

        Assert.assertEquals(content.trim(), "Just some content");
    }

    /**
     * Test that the connection handler does not try to open a URL with a protocol it does not understand.
     * @throws IOException expected
     */
    @Test (expectedExceptions = java.io.IOException.class)
    public void testUnknownResource()
            throws IOException {
        Handler handler = new Handler();
        Assert.assertNotNull(handler);

        // ask for a resource that does not exist
        URLConnection conn = handler.openConnection(URLFactory.create("rsc:doesnotexist.txt"));
    }

    /**
     * Test that the connection handler does not try to open a URL with a protocol it does not understand.
     * @throws IOException expected
     */
    @Test (expectedExceptions = java.io.IOException.class)
    public void testUnknownProtocolConnection()
            throws IOException {
        Handler handler = new Handler();
        Assert.assertNotNull(handler);

        URLConnection conn = handler.openConnection(URLFactory.create("http://www.google.com"));
        Assert.assertNotNull(conn);
    }
}
