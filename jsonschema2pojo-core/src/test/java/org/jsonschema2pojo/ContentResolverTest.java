/*
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.util.LocalHttpServerBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import static org.hamcrest.Matchers.*;

public class ContentResolverTest {

    private ContentResolver resolver = new ContentResolver(); 
    
    @Test(expected=IllegalArgumentException.class)
    public void wrongProtocolCausesIllegalArgumentException() {

        URI uriWithUnrecognisedProtocol = URI.create("foobar://schema/address.json"); 
        resolver.resolve(uriWithUnrecognisedProtocol);
    }

    private static LocalHttpServerBuilder.Server server = null;

    @BeforeClass
    public static void beforeClass() throws Exception{
        server = LocalHttpServerBuilder.createServer(
                LocalHttpServerBuilder.context("/address", "application/json", "utf-8",
                        IOUtils.toByteArray(ContentResolverTest.class.getResourceAsStream("/schema/address.json")))
        );
        server.startInRange(1024, 100);
    }

    @AfterClass
    public static void  afterClass() throws Exception{
        if(server!=null)
            server.close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void brokenLinkCausesIllegalArgumentException() {

        URI brokenHttpUri = URI.create("http://localhost:" + server.getPort() + "/sserdda");
        resolver.resolve(brokenHttpUri);
    }
    
    @Test
    public void httpLinkIsResolvedToContent() {

        URI httpUri = URI.create("http://localhost:" + server.getPort() + "/address");
        JsonNode uriContent = resolver.resolve(httpUri);
        
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));
    }

    @Test
    public void fileLinkIsResolvedToContent() throws IOException {
        
        URI schemaFile = createSchemaFile();
        
        JsonNode uriContent = resolver.resolve(schemaFile);
        
        assertThat(uriContent.path("type").asText(), is(equalTo("string")));
    }

    @Test
    public void classpathLinkIsResolvedToContent() throws IOException {
        
        URI schemaFile;
        JsonNode uriContent;
        
        schemaFile = URI.create("classpath:schema/address.json");
        uriContent = resolver.resolve(schemaFile);
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));

        schemaFile = URI.create("classpath:/schema/address.json");
        uriContent = resolver.resolve(schemaFile);
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));

        schemaFile = URI.create("resource:schema/address.json");
        uriContent = resolver.resolve(schemaFile);
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));

        schemaFile = URI.create("java:schema/address.json");
        uriContent = resolver.resolve(schemaFile);
        assertThat(uriContent.path("description").asText().length(), is(greaterThan(0)));

    }

    private URI createSchemaFile() throws IOException {
        File tempFile = File.createTempFile("jsonschema2pojotest", "json");
        tempFile.deleteOnExit();
        
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempFile);
            outputStream.write("{\"type\" : \"string\"}".getBytes("utf-8"));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        
        return tempFile.toURI();
    }
    
}
