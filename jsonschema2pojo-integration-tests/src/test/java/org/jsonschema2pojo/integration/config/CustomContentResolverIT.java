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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import org.joda.time.LocalDate;
import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.FormatRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

public class CustomContentResolverIT {

    @org.junit.Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void customContentResolverIsAbelToResolveUrn() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/extends/subtypeOfUrn.json", "com.example",
                config("customContentResolver", TestContentResolver.class.getName()));

        Class subtype = resultsClassLoader.loadClass("com.example.SubtypeOfUrn");
        Class supertype = resultsClassLoader.loadClass("com.example.A");

        assertThat(subtype.getSuperclass(), is(equalTo(supertype)));
    }

    public static class TestContentResolver extends ContentResolver {
        public TestContentResolver(JsonFactory factory, GenerationConfig config) {
            super(factory, config);
        }

        public TestContentResolver(GenerationConfig config) {
            super(config);
        }

        Map<URI, URI> map = Map.of(URI.create("urn:jsonschema2pojo:it:a"), URI.create("/schema/extends/a.json"));

        @Override
        public JsonNode resolve(URI uri) {
            if (map.get(uri) != null) {
                return super.resolve(map.get(uri));
            }

            return super.resolve(uri);
        }
    }
}
