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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class TypeRuleTest {

    private RuleFactory ruleFactory = createMock(RuleFactory.class);

    private TypeRule rule = new TypeRule(ruleFactory);

    @Before 
    public void clearSchemaCache() {
        Schema.clearCache();
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
        expect(mockFormatRule.apply("fooBar", formatNode, jpackage, null)).andReturn(mockDateType);
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

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(int.class.getName()));
    }

    @Test
    public void applyGeneratesNumber() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(double.class.getName()));
    }

    @Test
    public void applyGeneratesBoolean() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        assertThat(result.fullName(), is(boolean.class.getName()));
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

    @Test(expected = GenerationException.class)
    public void applyFailsOnUnrecognizedType() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        rule.apply("fooBar", objectNode, jpackage, null);
    }

}
