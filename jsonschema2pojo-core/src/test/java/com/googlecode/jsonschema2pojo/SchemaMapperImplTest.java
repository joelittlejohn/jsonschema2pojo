/**
 * Copyright © 2010 Nokia
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

package com.googlecode.jsonschema2pojo;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.easymock.Capture;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.rules.EnumRule;
import com.googlecode.jsonschema2pojo.rules.SchemaRule;
import com.googlecode.jsonschema2pojo.rules.TypeRule;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class SchemaMapperImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        SchemaMapper schemaMapper = new SchemaMapperImpl(null);

        assertThat(schemaMapper.getArrayRule(), notNullValue());

        assertThat(schemaMapper.getDescriptionRule(), notNullValue());

        assertThat(schemaMapper.getEnumRule(), notNullValue());

        assertThat(schemaMapper.getObjectRule(), notNullValue());

        assertThat(schemaMapper.getOptionalRule(), notNullValue());

        assertThat(schemaMapper.getPropertiesRule(), notNullValue());

        assertThat(schemaMapper.getPropertyRule(), notNullValue());

        assertThat(schemaMapper.getTypeRule(), notNullValue());

        assertThat(schemaMapper.getPropertiesRule(), notNullValue());

    }

    @Test
    public void generateReadsSchemaAsObject() throws IOException {

        final TypeRule mockTypeRule = createMock(TypeRule.class);
        SchemaMapper schemaMapper = new SchemaMapperImpl(null) {
            @Override
            public SchemaRule<JPackage, JType> getTypeRule() {
                return mockTypeRule;
            }
        };

        Capture<JPackage> capturePackage = new Capture<JPackage>();
        Capture<JsonNode> captureNode = new Capture<JsonNode>();

        expect(mockTypeRule.apply(eq("Address"), capture(captureNode), capture(capturePackage))).andReturn(null);

        InputStream schemaContent = this.getClass().getResourceAsStream("/schema/address.json");

        replay(mockTypeRule);

        schemaMapper.generate(new JCodeModel(), "Address", "com.example.package", schemaContent);

        verify(mockTypeRule);

        assertThat(capturePackage.hasCaptured(), is(true));
        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.hasCaptured(), is(true));
        assertThat(captureNode.getValue().get("description").getTextValue(), is("An Address following the convention of http://microformats.org/wiki/hcard"));

    }

    @Test
    public void generateReadsSchemaWithIdAsObject() throws IOException {

        final TypeRule mockTypeRule = createMock(TypeRule.class);
        SchemaMapper schemaMapper = new SchemaMapperImpl(null) {
            @Override
            public SchemaRule<JPackage, JType> getTypeRule() {
                return mockTypeRule;
            }
        };

        Capture<JPackage> capturePackage = new Capture<JPackage>();
        Capture<JsonNode> captureNode = new Capture<JsonNode>();

        expect(mockTypeRule.apply(eq("com.id.Address"), capture(captureNode), capture(capturePackage))).andReturn(null);

        InputStream schemaContent = this.getClass().getResourceAsStream("/schema/addressWithId.json");

        replay(mockTypeRule);

        schemaMapper.generate(new JCodeModel(), "IgnoredName", "com.example.package", schemaContent);

        verify(mockTypeRule);

        assertThat(capturePackage.hasCaptured(), is(true));
        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.hasCaptured(), is(true));
        assertThat(captureNode.getValue().get("description").getTextValue(), is("An Address following the convention of http://microformats.org/wiki/hcard"));

    }

    @Test
    public void generateReadsEnumSchemaAsEnum() throws IOException {

        final EnumRule mockEnumRule = createMock(EnumRule.class);
        SchemaMapper schemaMapper = new SchemaMapperImpl(null) {
            @Override
            public SchemaRule<JClassContainer, JDefinedClass> getEnumRule() {
                return mockEnumRule;
            }
        };

        Capture<JPackage> capturePackage = new Capture<JPackage>();
        Capture<JsonNode> captureNode = new Capture<JsonNode>();

        expect(mockEnumRule.apply(eq("Enum"), capture(captureNode), capture(capturePackage))).andReturn(null);

        InputStream schemaContent = this.getClass().getResourceAsStream("/schema/enum.json");

        replay(mockEnumRule);

        schemaMapper.generate(new JCodeModel(), "Enum", "com.example.package", schemaContent);

        verify(mockEnumRule);

        assertThat(capturePackage.hasCaptured(), is(true));
        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.hasCaptured(), is(true));
        assertThat(captureNode.getValue().isArray(), is(true));
        assertThat(captureNode.getValue().get(0).getTextValue(), is("one"));

    }

    @Test
    public void nullPropertiesAvoidsNullPointer() {
        SchemaMapper schemaMapper = new SchemaMapperImpl(null);
        assertThat(schemaMapper.getBehaviourProperty("anything"), is(nullValue()));
    }

    @Test
    public void putAndGetProperties() {

        String key = "KEY";
        String value = "VALUE";

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(key, value);

        SchemaMapper schemaMapper = new SchemaMapperImpl(properties);

        assertThat(schemaMapper.getBehaviourProperty(key), is(value));

    }

}
