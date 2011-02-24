/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.rules;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.easymock.Capture;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public class JsonSchemaRuleTest {

    private static final String NODE_NAME = "nodeName";
    private static final String TARGET_CLASS_NAME = JsonSchemaRuleTest.class.getName() + ".DummyClass";

    private RuleFactory mockRuleFactory = createMock(RuleFactory.class);
    private JsonSchemaRule rule = new JsonSchemaRule(mockRuleFactory);

    @Test
    public void refsToOtherSchemasAreLoaded() throws URISyntaxException, JClassAlreadyExistsException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        ObjectNode schemaWithRef = new ObjectMapper().createObjectNode();
        schemaWithRef.put("$ref", schemaUri.toString());

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        TypeRule mockTypeRule = createMock(TypeRule.class);
        expect(mockRuleFactory.getTypeRule()).andReturn(mockTypeRule);

        Capture<JsonNode> captureJsonNode = new Capture<JsonNode>();
        Capture<Schema> captureSchema = new Capture<Schema>();

        expect(mockTypeRule.apply(eq(NODE_NAME), capture(captureJsonNode), eq(jclass.getPackage()), capture(captureSchema))).andReturn(jclass);

        replay(mockTypeRule, mockRuleFactory);

        rule.apply(NODE_NAME, schemaWithRef, jclass, null);

        verify(mockTypeRule);

        assertThat(captureSchema.getValue().getId(), is(equalTo(schemaUri)));
        assertThat(captureSchema.getValue().getContent(), is(equalTo(captureJsonNode.getValue())));

        assertThat(captureJsonNode.getValue().get("description").getTextValue(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));
    }

    @Test
    public void enumAsRootIsGeneratedCorrectly() throws URISyntaxException, JClassAlreadyExistsException {

        ObjectNode schemaContent = new ObjectMapper().createObjectNode();
        ObjectNode enumNode = schemaContent.objectNode();
        enumNode.put("type", "string");
        schemaContent.put("enum", enumNode);

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        Schema schema = createMock(Schema.class);
        expect(schema.getContent()).andReturn(schemaContent).anyTimes();
        schema.setJavaTypeIfEmpty(jclass);

        EnumRule enumRule = createMock(EnumRule.class);
        expect(mockRuleFactory.getEnumRule()).andReturn(enumRule);

        expect(enumRule.apply(NODE_NAME, enumNode, jclass, schema)).andReturn(jclass);

        replay(schema, mockRuleFactory, enumRule);

        rule.apply(NODE_NAME, schemaContent, jclass, schema);

        verify(enumRule, schema);

    }

    @Test
    public void existingTypeIsUsedWhenTypeIsAlreadyGenerated() throws URISyntaxException {

        JType previouslyGeneratedType = createMock(JType.class);

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema = Schema.create(schemaUri);
        schema.setJavaType(previouslyGeneratedType);

        ObjectNode schemaNode = new ObjectMapper().createObjectNode();
        schemaNode.put("$ref", schemaUri.toString());

        JType result = rule.apply(NODE_NAME, schemaNode, null, schema);

        assertThat(result, is(sameInstance(previouslyGeneratedType)));

    }
}
