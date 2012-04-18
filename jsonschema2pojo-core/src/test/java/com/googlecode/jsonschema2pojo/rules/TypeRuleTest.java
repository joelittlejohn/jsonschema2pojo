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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.googlecode.jsonschema2pojo.GenerationConfig;
import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class TypeRuleTest {

    private GenerationConfig config = createNiceMock(GenerationConfig.class);
    private RuleFactory ruleFactory = createMock(RuleFactory.class);

    private TypeRule rule = new TypeRule(ruleFactory);

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Before
    public void wireUpConfig() {
        expect(ruleFactory.getGenerationConfig()).andReturn(config).anyTimes();
    }

    @Test
    public void applyGeneratesString() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(String.class.getName()));
    }

    @Test
    public void applyGeneratesDate() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        TextNode formatNode = TextNode.valueOf("date-time");
        objectNode.put("format", formatNode);

        JType mockDateType = createMock(JType.class);
        FormatRule mockFormatRule = createMock(FormatRule.class);
        expect(mockFormatRule.apply(eq("fooBar"), eq(formatNode), isA(JType.class), isNull(Schema.class))).andReturn(mockDateType);
        expect(ruleFactory.getFormatRule()).andReturn(mockFormatRule);

        replay(mockFormatRule, ruleFactory);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result, equalTo(mockDateType));
    }

    @Test
    public void applyGeneratesInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Integer.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        expect(config.isUsePrimitives()).andReturn(true);
        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesNumber() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Double.class.getName()));
    }

    @Test
    public void applyGeneratesNumberPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        expect(config.isUsePrimitives()).andReturn(true);
        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesBoolean() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Boolean.class.getName()));
    }

    @Test
    public void applyGeneratesBooleanPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        expect(config.isUsePrimitives()).andReturn(true);
        replay(ruleFactory, config);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is("boolean"));
    }

    @Test
    public void applyGeneratesAnyAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "any");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "null");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "array");

        JClass mockArrayType = createMock(JClass.class);
        ArrayRule mockArrayRule = createMock(ArrayRule.class);
        expect(mockArrayRule.apply("fooBar", objectNode, jpackage, null)).andReturn(mockArrayType);
        expect(ruleFactory.getArrayRule()).andReturn(mockArrayRule);

        replay(mockArrayRule, ruleFactory);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result, is((JType) mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "object");

        JDefinedClass mockObjectType = createMock(JDefinedClass.class);
        ObjectRule mockObjectRule = createMock(ObjectRule.class);
        expect(mockObjectRule.apply("fooBar", objectNode, jpackage, null)).andReturn(mockObjectType);
        expect(ruleFactory.getObjectRule()).andReturn(mockObjectRule);

        replay(mockObjectRule, ruleFactory);

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result, is((JType) mockObjectType));
    }

    @Test
    public void applyChoosesObjectOnUnrecognizedType() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));

    }

    @Test
    public void applyDefaultsToTypeAnyObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

}
