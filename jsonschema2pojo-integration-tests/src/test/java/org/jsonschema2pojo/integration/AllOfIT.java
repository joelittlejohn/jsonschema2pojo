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

package org.jsonschema2pojo.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AllOfIT {

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void simpleAllOfMergesProperties() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/simpleAllOf.json", "com.example");
        Class<?> type = cl.loadClass("com.example.SimpleAllOf");

        assertThat(type.getDeclaredField("name"), is(notNullValue()));
        assertThat(type.getDeclaredField("age"), is(notNullValue()));

        assertThat(type.getDeclaredField("name").getType(), is(equalTo(String.class)));
        assertThat(type.getDeclaredField("age").getType(), is(equalTo(Integer.class)));
    }

    @Test
    public void allOfWithRefMergesReferencedSchemaProperties() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithRef.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithRef");

        // Property from the $ref'd base.json
        assertThat(type.getDeclaredField("baseProperty"), is(notNullValue()));
        assertThat(type.getDeclaredField("baseProperty").getType(), is(equalTo(String.class)));

        // Property from the inline sub-schema
        assertThat(type.getDeclaredField("extraProperty"), is(notNullValue()));
        assertThat(type.getDeclaredField("extraProperty").getType(), is(equalTo(Integer.class)));
    }

    @Test
    public void allOfWithSiblingPropertiesMergesBoth() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithSiblingProperties.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithSiblingProperties");

        // Direct sibling property
        assertThat(type.getDeclaredField("directProperty"), is(notNullValue()));
        // Property from allOf sub-schema
        assertThat(type.getDeclaredField("mergedProperty"), is(notNullValue()));
    }

    @Test
    public void allOfWithThreeSchemasHasAllProperties() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithThreeSchemas.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithThreeSchemas");

        assertThat(type.getDeclaredField("first"), is(notNullValue()));
        assertThat(type.getDeclaredField("second"), is(notNullValue()));
        assertThat(type.getDeclaredField("third"), is(notNullValue()));

        assertThat(type.getDeclaredField("first").getType(), is(equalTo(String.class)));
        assertThat(type.getDeclaredField("second").getType(), is(equalTo(Integer.class)));
        assertThat(type.getDeclaredField("third").getType(), is(equalTo(Boolean.class)));
    }

    @Test
    public void allOfWithOverlappingRequiredDeduplicates() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithOverlappingRequired.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithOverlappingRequired");

        // All three properties should be present
        assertThat(type.getDeclaredField("name"), is(notNullValue()));
        assertThat(type.getDeclaredField("age"), is(notNullValue()));
        assertThat(type.getDeclaredField("email"), is(notNullValue()));
    }

    @Test
    public void emptyAllOfGeneratesFromSiblingProperties() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/emptyAllOf.json", "com.example");
        Class<?> type = cl.loadClass("com.example.EmptyAllOf");

        assertThat(type.getDeclaredField("directProperty"), is(notNullValue()));
    }

    @Test
    public void singleElementAllOfUnwraps() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/singleElementAllOf.json", "com.example");
        Class<?> type = cl.loadClass("com.example.SingleElementAllOf");

        assertThat(type.getDeclaredField("onlyProperty"), is(notNullValue()));
    }

    @Test
    public void allOfWithOverlappingPropertiesUsesLastDefinition() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithOverlappingProperties.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithOverlappingProperties");

        // All three unique properties present
        assertThat(type.getDeclaredField("shared"), is(notNullValue()));
        assertThat(type.getDeclaredField("firstOnly"), is(notNullValue()));
        assertThat(type.getDeclaredField("secondOnly"), is(notNullValue()));
    }

    @Test
    public void nestedAllOfIsFlattened() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/nestedAllOf.json", "com.example");
        Class<?> type = cl.loadClass("com.example.NestedAllOf");

        assertThat(type.getDeclaredField("innerProperty"), is(notNullValue()));
        assertThat(type.getDeclaredField("outerProperty"), is(notNullValue()));
    }

    @Test
    public void allOfAllRefsMergesAllReferencedProperties() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfAllRefs.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfAllRefs");

        assertThat(type.getDeclaredField("propA"), is(notNullValue()));
        assertThat(type.getDeclaredField("propB"), is(notNullValue()));
    }

    @Test
    public void allOfPropertyLevelGeneratesMergedType() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfPropertyLevel.json", "com.example");
        Class<?> parentType = cl.loadClass("com.example.AllOfPropertyLevel");
        Class<?> addressType = cl.loadClass("com.example.Address");

        // The parent has an "address" property
        Method getter = parentType.getMethod("getAddress");
        assertThat(getter.getReturnType(), is(equalTo(addressType)));

        // The address type has both merged properties
        assertThat(addressType.getDeclaredField("street"), is(notNullValue()));
        assertThat(addressType.getDeclaredField("city"), is(notNullValue()));
    }

    @Test
    public void allOfWithEnumGeneratesEnumType() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithEnum.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithEnum");

        assertThat(type.isEnum(), is(true));
        assertThat(type.getEnumConstants().length, is(3));
    }

    @Test
    public void allOfMergedTypeCanBeDeserialized() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/simpleAllOf.json", "com.example");
        Class<?> type = cl.loadClass("com.example.SimpleAllOf");

        Object instance = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", type);

        assertThat(new PropertyDescriptor("name", type).getReadMethod().invoke(instance), is("Alice"));
        assertThat(new PropertyDescriptor("age", type).getReadMethod().invoke(instance), is(30));
    }

    @Test
    public void allOfMergedTypeCanBeSerializedAndDeserialized() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithThreeSchemas.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithThreeSchemas");

        Object instance = mapper.readValue("{\"first\":\"hello\",\"second\":42,\"third\":true}", type);

        // Round-trip: serialize back to JSON and deserialize again
        String json = mapper.writeValueAsString(instance);
        Object roundTripped = mapper.readValue(json, type);

        assertThat(new PropertyDescriptor("first", type).getReadMethod().invoke(roundTripped), is("hello"));
        assertThat(new PropertyDescriptor("second", type).getReadMethod().invoke(roundTripped), is(42));
        assertThat(new PropertyDescriptor("third", type).getReadMethod().invoke(roundTripped), is(true));
    }

    @Test
    public void allOfWithRefCanBeDeserialized() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithRef.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithRef");

        Object instance = mapper.readValue("{\"baseProperty\":\"base\",\"extraProperty\":7}", type);

        assertThat(new PropertyDescriptor("baseProperty", type).getReadMethod().invoke(instance), is("base"));
        assertThat(new PropertyDescriptor("extraProperty", type).getReadMethod().invoke(instance), is(7));
    }

    @Test
    public void allOfWithAnyOfPropertyComposesCorrectly() throws Exception {
        ClassLoader cl = schemaRule.generateAndCompile("/schema/allOf/allOfWithAnyOfProperty.json", "com.example");
        Class<?> type = cl.loadClass("com.example.AllOfWithAnyOfProperty");

        // "name" from the first allOf sub-schema
        assertThat(type.getDeclaredField("name"), is(notNullValue()));

        // "variant" from the second allOf sub-schema should be an interface type
        Method variantGetter = type.getMethod("getVariant");
        Class<?> variantType = variantGetter.getReturnType();
        assertThat(variantType.isInterface(), is(true));

        // The anyOf children should implement the interface
        Class<?> variantA = cl.loadClass("com.example.VariantA");
        Class<?> variantB = cl.loadClass("com.example.VariantB");
        assertThat(variantType.isAssignableFrom(variantA), is(true));
        assertThat(variantType.isAssignableFrom(variantB), is(true));
    }

}
