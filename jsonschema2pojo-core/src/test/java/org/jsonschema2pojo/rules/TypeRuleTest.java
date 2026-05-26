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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jsonschema2pojo.GenerationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class TypeRuleTest {

    private final ValidRule validRule = mock(ValidRule.class);
    private final GenerationConfig config = mock(GenerationConfig.class);
    private final RuleFactory ruleFactory = mock(RuleFactory.class);

    private final TypeRule rule = new TypeRule(ruleFactory);

    @BeforeEach
    public void wireUpConfig() {
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
        when(ruleFactory.getValidRule()).thenReturn(validRule);
        when(validRule.apply(any(),any(),any(),any(),any())).then(AdditionalAnswers.returnsArgAt(3));
    }

    @Test
    public void applyGeneratesString() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(String.class.getName()));
    }

    @Test
    public void applyGeneratesDate() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        TextNode formatNode = TextNode.valueOf("date-time");
        objectNode.set("format", formatNode);

        JType mockDateType = mock(JType.class);
        FormatRule mockFormatRule = mock(FormatRule.class);
        when(mockFormatRule.apply(eq("fooBar"), eq(formatNode), any(), Mockito.isA(JType.class), isNull())).thenReturn(mockDateType);
        when(ruleFactory.getFormatRule()).thenReturn(mockFormatRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, equalTo(mockDateType));
    }

    @Test
    public void applyGeneratesInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Integer.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeIntegerPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "int");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesBigInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUseBigIntegers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigIntegerOverridingLong() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        // isUseBigIntegers should override isUseLongIntegers
        when(config.isUseBigIntegers()).thenReturn(true);
        when(config.isUseLongIntegers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimal() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseBigDecimals()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimalOverridingDouble() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        //this shows that isUseBigDecimals overrides isUseDoubleNumbers
        when(config.isUseDoubleNumbers()).thenReturn(true);
        when(config.isUseBigDecimals()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }


    @Test
    public void applyGeneratesIntegerUsingJavaTypeInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Integer");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Integer"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "long");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLong() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Long");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumLessThanIntegerMin() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumGreaterThanIntegerMax() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeBigInteger() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.math.BigInteger");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigInteger"));
    }

    @Test
    public void applyGeneratesNumber() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseDoubleNumbers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Double.class.getName()));
    }

    @Test
    public void applyGeneratesNumberPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloatPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "float");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloat() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Float");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDoublePrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "double");

        when(config.isUsePrimitives()).thenReturn(false);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDouble() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Double");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeBigDecimal() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.math.BigDecimal");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigDecimal"));
    }

    @Test
    public void applyGeneratesBoolean() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Boolean.class.getName()));
    }

    @Test
    public void applyGeneratesBooleanPrimitive() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        when(config.isUsePrimitives()).thenReturn(true);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("boolean"));
    }

    @Test
    public void applyGeneratesAnyAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "any");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "null");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "array");

        JClass mockArrayType = mock(JClass.class);
        ArrayRule mockArrayRule = mock(ArrayRule.class);
        when(mockArrayRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockArrayType);
        when(ruleFactory.getArrayRule()).thenReturn(mockArrayRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is(mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "object");

        JDefinedClass mockObjectType = mock(JDefinedClass.class);
        ObjectRule mockObjectRule = mock(ObjectRule.class);
        when(mockObjectRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockObjectType);
        when(ruleFactory.getObjectRule()).thenReturn(mockObjectRule);

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is(mockObjectType));
    }

    @Test
    public void applyChoosesObjectOnUnrecognizedType() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));

    }

    @Test
    public void applyDefaultsToTypeAnyObject() {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        JType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

}
