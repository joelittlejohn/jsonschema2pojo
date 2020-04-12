/**
 * Copyright © 2010-2020 Nokia
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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContentResolverTest {

    private ContentResolver resolver = new ContentResolver();
    
    @Test(expected=IllegalArgumentException.class)
    public void wrongProtocolCausesIllegalArgumentException() {

        URI uriWithUnrecognisedProtocol = URI.create("foobar://schema/address.json"); 
        resolver.resolve(uriWithUnrecognisedProtocol);
    }

    @Test
    public void fileLinkIsResolvedToContent() throws IOException {
        
        URI schemaFile = createSchemaFile();
        
        JsonNode uriContent = resolver.resolve(schemaFile);
        
        assertThat(uriContent.path("type").asText(), is(equalTo("string")));
    }

    @Test
    public void classpathLinkIsResolvedToContent() {
        
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

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            outputStream.write("{\"type\" : \"string\"}".getBytes("utf-8"));
        }
        
        return tempFile.toURI();
    }
    
}
