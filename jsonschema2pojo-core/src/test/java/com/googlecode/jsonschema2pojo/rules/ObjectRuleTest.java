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

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;

public class ObjectRuleTest {

    private static final String TARGET_PACKAGE_NAME = ArrayRuleTest.class.getPackage().getName() + ".test";

    private static final String EXPECTED_RESULT =
            "@javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "public class FooBar\n" +
                    "    implements java.io.Serializable\n{\n\n\n" +
                    "    @java.lang.Override\n" +
                    "    public java.lang.String toString() {\n" +
                    "        return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public int hashCode() {\n" +
                    "        return org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public boolean equals(java.lang.Object other) {\n" +
                    "        return org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals(this, other);\n" +
                    "    }\n\n" +
                    "}\n";

    private static final String EXPECTED_NAME_RESULT =
            "@javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "public class MyJavaType\n" +
                    "    implements java.io.Serializable\n{\n\n\n" +
                    "    @java.lang.Override\n" +
                    "    public java.lang.String toString() {\n" +
                    "        return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public int hashCode() {\n" +
                    "        return org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public boolean equals(java.lang.Object other) {\n" +
                    "        return org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals(this, other);\n" +
                    "    }\n\n" +
                    "}\n";

    private RuleFactory mockRuleFactory = createMock(RuleFactory.class);
    private ObjectRule rule = new ObjectRule(mockRuleFactory);

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    public void applyGeneratesBean() {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        AdditionalPropertiesRule mockAdditionalPropertiesRule = createMock(AdditionalPropertiesRule.class);
        expect(mockRuleFactory.getAdditionalPropertiesRule()).andReturn(mockAdditionalPropertiesRule);

        Schema mockSchema = createMock(Schema.class);
        mockSchema.setJavaTypeIfEmpty(isA(JDefinedClass.class));

        replay(mockSchema, mockRuleFactory);

        JDefinedClass result = rule.apply("fooBar", objectNode, jpackage, mockSchema);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT));
    }

    @Test
    public void applyGeneratesBeanWithExplicitTypeName() {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);
        String className = TARGET_PACKAGE_NAME + ".MyJavaType";

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("javaType", className);

        AdditionalPropertiesRule mockAdditionalPropertiesRule = createMock(AdditionalPropertiesRule.class);
        expect(mockRuleFactory.getAdditionalPropertiesRule()).andReturn(mockAdditionalPropertiesRule);

        Schema mockSchema = createMock(Schema.class);
        mockSchema.setJavaTypeIfEmpty(isA(JDefinedClass.class));

        replay(mockSchema, mockRuleFactory);

        JDefinedClass result = rule.apply("fooBar", objectNode, jpackage, mockSchema);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_NAME_RESULT));
    }

    @Test
    public void applyGeneratesWithAdditionalNodes() {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode descriptionNode = objectMapper.createObjectNode();
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        ObjectNode additionalPropertiesNode = objectMapper.createObjectNode();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("description", descriptionNode);
        objectNode.put("properties", propertiesNode);
        objectNode.put("additionalProperties", additionalPropertiesNode);

        DescriptionRule mockDescriptionRule = createMock(DescriptionRule.class);
        PropertiesRule mockPropertiesRule = createMock(PropertiesRule.class);
        AdditionalPropertiesRule mockAdditionalPropertiesRule = createMock(AdditionalPropertiesRule.class);

        Schema mockSchema = createMock(Schema.class);
        mockSchema.setJavaTypeIfEmpty(isA(JDefinedClass.class));

        expect(mockDescriptionRule.apply(eq("fooBar"), eq(descriptionNode), isA(JDefinedClass.class), eq(mockSchema))).andReturn(null);
        expect(mockPropertiesRule.apply(eq("fooBar"), eq(propertiesNode), isA(JDefinedClass.class), eq(mockSchema))).andReturn(null);
        expect(mockAdditionalPropertiesRule.apply(eq("fooBar"), eq(additionalPropertiesNode), isA(JDefinedClass.class), eq(mockSchema))).andReturn(null);

        expect(mockRuleFactory.getDescriptionRule()).andReturn(mockDescriptionRule);
        expect(mockRuleFactory.getPropertiesRule()).andReturn(mockPropertiesRule);
        expect(mockRuleFactory.getAdditionalPropertiesRule()).andReturn(mockAdditionalPropertiesRule);

        replay(mockRuleFactory, mockDescriptionRule, mockPropertiesRule, mockAdditionalPropertiesRule);

        JDefinedClass result = rule.apply("fooBar", objectNode, jpackage, mockSchema);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT));

        verify(mockDescriptionRule, mockPropertiesRule, mockAdditionalPropertiesRule);

    }

    @Test
    public void applyReturnsExistingClassWhenConflictOccurs() throws JClassAlreadyExistsException {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        JDefinedClass existingClass = jpackage._class("ExistingClass");

        JDefinedClass result = rule.apply("existingClass", new ObjectMapper().createObjectNode(), jpackage, createMock(Schema.class));

        assertThat(result, is(equalTo(existingClass)));
    }
}
