package org.jsonschema2pojo.util;

import org.jsonschema2pojo.URLProtocol;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class URLUtilTest {

    @Test
    public void testParseProtocol() {
        assertThat(URLUtil.parseProtocol("https://testhttps.protocol"), is(URLProtocol.HTTPS));
        assertThat(URLUtil.parseProtocol("http://testhttp.protocol"), is(URLProtocol.HTTP));
        assertThat(URLUtil.parseProtocol("file:/testfile/protocol"), is(URLProtocol.FILE));
        assertThat(URLUtil.parseProtocol("classpath:/test/classpath/class.java"), is(URLProtocol.CLASSPATH));
        assertThat(URLUtil.parseProtocol("java:test/java"), is(URLProtocol.JAVA));
        assertThat(URLUtil.parseProtocol("resource:test/resource"), is(URLProtocol.RESOURCE));
        assertThat(URLUtil.parseProtocol("there are no protocol: at all"), is(URLProtocol.NO_PROTOCOL));
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
    public void testParseThrowsExceptionWhenURLIsMalformed() {
        URLUtil.parseURL("file:@#$12#42");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testExceptionMessageWhenParseURL() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to parse source: " + "java:@$1242");
        URLUtil.parseURL("java:@$1242");
    }

    @Test
    public void testGetFileFromURL() throws MalformedURLException {
        File file = new File(File.pathSeparator + "testResource");
        URL url = file.toURI().toURL();
        assertThat(file, not(equalTo(URLUtil.getFileFromURL(url))));
        assertThat(file.getAbsolutePath(), equalTo(URLUtil.getFileFromURL(url).getAbsolutePath()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFileFromUrlThrowsExceptionWhenURLIsMalformed() throws MalformedURLException {
        URL url = new URL("http://finance.yahoo.com/q/h?s=^IXIC");
        URLUtil.getFileFromURL(url);
    }


}
