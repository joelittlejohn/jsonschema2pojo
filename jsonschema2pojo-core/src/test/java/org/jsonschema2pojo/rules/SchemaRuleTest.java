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

package org.jsonschema2pojo.rules;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public class SchemaRuleTest {

    private static final String NODE_NAME = "nodeName";
    private static final String TARGET_CLASS_NAME = SchemaRuleTest.class.getName() + ".DummyClass";

    private RuleFactory mockRuleFactory = mock(RuleFactory.class);
    private SchemaRule rule = new SchemaRule(mockRuleFactory);

    @Test
    public void refsToOtherSchemasAreLoaded() throws URISyntaxException, JClassAlreadyExistsException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        ObjectNode schemaWithRef = new ObjectMapper().createObjectNode();
        schemaWithRef.put("$ref", schemaUri.toString());

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getRefFragmentPathDelimiters()).thenReturn("#/.");

        TypeRule mockTypeRule = mock(TypeRule.class);
        when(mockRuleFactory.getTypeRule()).thenReturn(mockTypeRule);
        when(mockRuleFactory.getSchemaStore()).thenReturn(new SchemaStore());
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        ArgumentCaptor<JsonNode> captureJsonNode = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<Schema> captureSchema = ArgumentCaptor.forClass(Schema.class);

        rule.apply(NODE_NAME, schemaWithRef, null, jclass, null);

        verify(mockTypeRule).apply(eq("address"), captureJsonNode.capture(), any(), eq(jclass.getPackage()), captureSchema.capture());

        assertThat(captureSchema.getValue().getId(), is(equalTo(schemaUri)));
        assertThat(captureSchema.getValue().getContent(), is(equalTo(captureJsonNode.getValue())));

        assertThat(captureJsonNode.getValue().get("description").asText(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));
    }

    @Test
    public void enumAsRootIsGeneratedCorrectly() throws JClassAlreadyExistsException {

        ObjectNode schemaContent = new ObjectMapper().createObjectNode();
        ObjectNode enumNode = schemaContent.objectNode();
        enumNode.put("type", "string");
        schemaContent.set("enum", enumNode);

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        Schema schema = mock(Schema.class);
        when(schema.getContent()).thenReturn(schemaContent);
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        schema.setJavaTypeIfEmpty(jclass);

        EnumRule enumRule = mock(EnumRule.class);
        when(mockRuleFactory.getEnumRule()).thenReturn(enumRule);

        when(enumRule.apply(NODE_NAME, enumNode, null, jclass, schema)).thenReturn(jclass);

        rule.apply(NODE_NAME, schemaContent, null, jclass, schema);

        verify(enumRule).apply(NODE_NAME, schemaContent, null, jclass, schema);
        verify(schema, atLeastOnce()).setJavaTypeIfEmpty(jclass);

    }

    @Test
    public void existingTypeIsUsedWhenTypeIsAlreadyGenerated() throws URISyntaxException {

        JType previouslyGeneratedType = mock(JType.class);

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(schemaUri, "#/.");
        schema.setJavaType(previouslyGeneratedType);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getRefFragmentPathDelimiters()).thenReturn("#/.");

        when(mockRuleFactory.getSchemaStore()).thenReturn(schemaStore);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        ObjectNode schemaNode = new ObjectMapper().createObjectNode();
        schemaNode.put("$ref", schemaUri.toString());

        JType result = rule.apply(NODE_NAME, schemaNode, null,null, schema);

        assertThat(result, is(sameInstance(previouslyGeneratedType)));

    }
}
