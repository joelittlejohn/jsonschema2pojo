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

package com.googlecode.jsonschema2pojo.rules;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public class TypeRuleTest {

    private static final String TARGET_CLASS_NAME = ArrayRuleTest.class.getName() + ".DummyClass";

    private SchemaMapper mockSchemaMapper = createMock(SchemaMapper.class);

    private TypeRule rule = new TypeRule(mockSchemaMapper);

    @Test
    public void applyGeneratesEnum() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode enumNode = objectMapper.createArrayNode();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("enum", enumNode);
        objectNode.put("type", "string");

        JDefinedClass mockEnumType = createMock(JDefinedClass.class);
        EnumRule mockEnumRule = createMock(EnumRule.class);
        expect(mockEnumRule.apply("fooBar", enumNode, jclass)).andReturn(mockEnumType);
        expect(mockSchemaMapper.getEnumRule()).andReturn(mockEnumRule);

        replay(mockEnumRule, mockSchemaMapper);

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result, is((JType) mockEnumType));
    }

    @Test
    public void applyGeneratesString() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(String.class.getName()));
    }

    @Test
    public void applyGeneratesDate() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");
        objectNode.put("format", "date-time");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(Date.class.getName()));
    }

    @Test
    public void applyGeneratesInteger() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(int.class.getName()));
    }

    @Test
    public void applyGeneratesNumber() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(double.class.getName()));
    }

    @Test
    public void applyGeneratesBoolean() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(boolean.class.getName()));
    }

    @Test
    public void applyGeneratesAnyAsObject() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "any");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "null");

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "array");

        JClass mockArrayType = createMock(JClass.class);
        ArrayRule mockArrayRule = createMock(ArrayRule.class);
        expect(mockArrayRule.apply("fooBar", objectNode, jclass)).andReturn(mockArrayType);
        expect(mockSchemaMapper.getArrayRule()).andReturn(mockArrayRule);

        replay(mockArrayRule, mockSchemaMapper);

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result, is((JType) mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "object");

        JDefinedClass mockObjectType = createMock(JDefinedClass.class);
        ObjectRule mockObjectRule = createMock(ObjectRule.class);
        expect(mockObjectRule.apply("fooBar", objectNode, jclass._package())).andReturn(mockObjectType);
        expect(mockSchemaMapper.getObjectRule()).andReturn(mockObjectRule);

        replay(mockObjectRule, mockSchemaMapper);

        JType result = rule.apply("fooBar", objectNode, jclass);

        assertThat(result, is((JType) mockObjectType));
    }

    @Test(expected = GenerationException.class)
    public void applyFailsOnUnrecognizedType() throws JClassAlreadyExistsException {

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        rule.apply("fooBar", objectNode, jclass);
    }

}
