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

import static java.util.Arrays.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Strings;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads URI contents for various protocols.
 */
public class ContentResolver {

    private static final Set<String> CLASSPATH_SCHEMES = new HashSet<>(asList("classpath", "resource", "java"));
    
    private final ObjectMapper objectMapper;

    public ContentResolver() {
        this(null);
    }

    public ContentResolver(JsonFactory jsonFactory) {
        this.objectMapper = new ObjectMapper(jsonFactory)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    /**
     * Resolve a given URI to read its contents and parse the result as JSON.
     * <p>
     * Supported protocols:
     * <ul>
     * <li>http/https
     * <li>file
     * <li>classpath/resource/java (all synonymous, used to resolve a schema
     * from the classpath)
     * </ul>
     *
     * @param uri
     *            the URI to read schema content from
     * @return the JSON tree found at the given URI
     */
    public JsonNode resolve(URI uri) {

        if (CLASSPATH_SCHEMES.contains(uri.getScheme())) {
            return resolveFromClasspath(uri);
        }

        try (InputStream in = uri.toURL().openStream()){
            return objectMapper.readTree(in);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing document: " + uri, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unrecognised URI, can't resolve this: " + uri, e);
        }

    }

    private JsonNode resolveFromClasspath(URI uri) {

        String path = Strings.CS.removeStart(Strings.CS.removeStart(uri.toString(), uri.getScheme() + ":"), "/");
        InputStream contentAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (contentAsStream == null) {
            throw new IllegalArgumentException("Couldn't read content from the classpath, file not found: " + uri);
        }

        try {
            return objectMapper.readTree(contentAsStream);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing document: " + uri, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unrecognised URI, can't resolve this: " + uri, e);
        }
    }

}
