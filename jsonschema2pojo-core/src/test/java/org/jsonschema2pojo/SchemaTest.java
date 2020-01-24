/**
 * Copyright © 2010-2017 Nokia
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

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaTest {

    private Schema schema;
    private Map<String, JType> typeCache;
    private String expectedShaKey;

    public SchemaTest() throws URISyntaxException {
        typeCache = new HashMap<>();
        ContentResolver resolver = new ContentResolver();
        URI schemaUri = getClass().getResource("/schema/repeatedSubschemas.json").toURI();
        JsonNode jsonNode = resolver.resolve(schemaUri);

        expectedShaKey = "feccccb727100a4ce3318a631bb6c2045039c3aa01904dc35aaf9a398b154b87";
        schema = new Schema(schemaUri, jsonNode, null, typeCache::put);
    }

    @Test
    public void givenSchemaThenShaKeyIsNotNull() {
        assertThat(schema.getSha256key(), notNullValue());
    }

    @Test
    public void givenContentThenExpectShaKey() {
        assertThat(schema.getSha256key(), is(expectedShaKey));
    }

    @Test
    public void givenChildSchemaWhenSettingTypeThenTypeIsAddedToCache() throws JClassAlreadyExistsException {
        JsonNode jsonNode = schema.getContent().get("properties").get("frontRightWheel");
        Schema child = schema.deriveChildSchema(jsonNode);
        JType type = new JCodeModel()._package("com.example")._class("Wheel");

        child.setJavaTypeIfEmpty(type);

        assertThat(typeCache.size(), is(1));
        assertTrue(typeCache.containsKey(child.getSha256key()));
        assertThat(typeCache.get(child.getSha256key()), sameInstance(type));
    }

}
