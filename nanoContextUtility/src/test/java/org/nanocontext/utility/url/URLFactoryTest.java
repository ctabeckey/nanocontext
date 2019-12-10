package org.nanocontext.utility.url;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests for the URLFactory
 */
public class URLFactoryTest {
    /**
     * Valid URLs
     * @return String[][] a 2d array of stimulus and expected results
     */
    @DataProvider
    public Object[][] validUrls() {
        return new Object[][] {
            new Object[]{null},
            new Object[]{"rsc:junk.txt"},
            new Object[]{"http://www.nanocontext.org"},
            new Object[]{"ftp://www.nanocontext.org/junk"},
            new Object[]{"file://User"},
        };
    }

    /**
     * Invalid URLs
     * @return String[][] a 2d array of stimulus and expected results.
     * The expected results are ignored.
     */
    @DataProvider
    public Object[][] invalidUrls() {
        return new Object[][] {
                new Object[]{"xyz:junk.txt", null},
                new Object[]{"", "Zero length URL is not valid."},
                new Object[]{"junk.txt", null},
                new Object[]{"®©://¬˜nanocontext.org", null},      // include invalid characters for a scheme
        };
    }

    /**
     * Test that valid URLs can be created
     *
     * @param stimulus a valid URL
     * @throws IOException thrown when a URL is invalid, should never be thrown in this test
     */
    @Test(dataProvider = "validUrls", singleThreaded = true)
    public void testValidUrls(String stimulus)
            throws java.io.IOException {
        URL url = URLFactory.create(stimulus);
        if (stimulus == null) {
            Assert.assertNull(url);
        } else {
            Assert.assertNotNull(url);
            Assert.assertEquals(stimulus, url.toString());
        }
    }

    /**
     * test that invalid URLs are not accepted
     * @param stimulus an invalid URL as a String
     * @throws IOException MalformedURLException (a derivative of this class) should always be thrown in this test
     */
    @Test(dataProvider = "invalidUrls", singleThreaded = true)
    public void testInvalidRsc(String stimulus, String expectedMessage)
            throws IOException {
        // should throw an exception for all test stimulus
        try {
            URL url = URLFactory.create(stimulus);
            Assert.assertNotNull(url);  // should never be reached, here to assure that the unused variable is not optimized away
            Assert.fail("Expected MalformedURLException was not thrown.");
        } catch (MalformedURLException murlX) {
            if (expectedMessage != null) {
                Assert.assertEquals(murlX.getMessage(), expectedMessage);
            }
        }
    }
}
