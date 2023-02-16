package org.jsonschema2pojo.util;

import org.jsonschema2pojo.URLProtocol;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLUtilTest {

    @Test
    public void testParseProtocol() {
        Assert.assertEquals(URLProtocol.HTTPS, URLUtil.parseProtocol("https://testhttps.protocol"));
        Assert.assertEquals(URLProtocol.HTTP, URLUtil.parseProtocol("http://testhttp.protocol"));
        Assert.assertEquals(URLProtocol.FILE, URLUtil.parseProtocol("file:/testfile/protocol"));
        Assert.assertEquals(URLProtocol.CLASSPATH, URLUtil.parseProtocol("classpath:/test/classpath/class.java"));
        Assert.assertEquals(URLProtocol.JAVA, URLUtil.parseProtocol("java:test/java"));
        Assert.assertEquals(URLProtocol.RESOURCE, URLUtil.parseProtocol("resource:test/resource"));
        Assert.assertEquals(URLProtocol.NO_PROTOCOL, URLUtil.parseProtocol("there are no protocol: at all"));
    }

    @Test
    public void testParseUrlNoProtocol() throws MalformedURLException {
        String url = "This: not url at all";
        File file = new File(url);
        URL expectedURL = file.toURI().toURL();
        Assert.assertEquals(expectedURL, URLUtil.parseURL(url));
    }

    @Test
    public void testParseUrlDefault() throws MalformedURLException {
        String url = "file:this/is/url";
        Assert.assertEquals(new URL(url), URLUtil.parseURL(url));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlException() {
        URLUtil.parseURL("file:@#$12#42");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test2() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to parse source: " + "java:@$1242");
        URLUtil.parseURL("java:@$1242");
    }

    @Test
    public void testGetFileFromUrlCorrect() throws MalformedURLException {
        File file = new File(File.pathSeparatorChar + "testResource");
        URI uri = file.toURI();
        URL url = uri.toURL();
        Assert.assertNotEquals(file, URLUtil.getFileFromURL(url));
        Assert.assertEquals(file.getAbsolutePath(), URLUtil.getFileFromURL(url).getAbsolutePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFileFromUrlException() throws MalformedURLException {
        URL url = new URL("http://finance.yahoo.com/q/h?s=^IXIC");
        URLUtil.getFileFromURL(url);
    }


}
