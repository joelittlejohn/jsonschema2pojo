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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JCodeModelException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;
import org.jsonschema2pojo.GenerationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class TypeRuleTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final RuleFactory ruleFactory = mock(RuleFactory.class);

    private final TypeRule rule = new TypeRule(ruleFactory);

    @BeforeEach
    public void wireUpConfig() {
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
    }

    @Test
    public void applyGeneratesString() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(String.class.getName()));
    }

    @Test
    public void applyGeneratesDate() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "string");

        TextNode formatNode = TextNode.valueOf("date-time");
        objectNode.set("format", formatNode);

        AbstractJType mockDateType = mock(AbstractJType.class);
        FormatRule mockFormatRule = mock(FormatRule.class);
        when(mockFormatRule.apply(eq("fooBar"), eq(formatNode), any(), Mockito.isA(AbstractJType.class), isNull())).thenReturn(mockDateType);
        when(ruleFactory.getFormatRule()).thenReturn(mockFormatRule);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, equalTo(mockDateType));
    }

    @Test
    public void applyGeneratesInteger() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Integer.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeIntegerPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "int");

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesBigInteger() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        when(config.isUseBigIntegers()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigIntegerOverridingLong() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");

        // isUseBigIntegers should override isUseLongIntegers
        when(config.isUseBigIntegers()).thenReturn(true);
        when(config.isUseLongIntegers()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigInteger.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimal() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseBigDecimals()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }

    @Test
    public void applyGeneratesBigDecimalOverridingDouble() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        //this shows that isUseBigDecimals overrides isUseDoubleNumbers
        when(config.isUseDoubleNumbers()).thenReturn(true);
        when(config.isUseBigDecimals()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(BigDecimal.class.getName()));
    }


    @Test
    public void applyGeneratesIntegerUsingJavaTypeInteger() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Integer");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Integer"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "long");

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLong() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.lang.Long");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumGreaterThanIntegerMax() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumGreaterThanIntegerMax() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumLessThanIntegerMin() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumLessThanIntegerMin() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumLessThanIntegerMin() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumLessThanIntegerMin() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumGreaterThanIntegerMax() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumGreaterThanIntegerMax() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeBigInteger() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "integer");
        objectNode.put("existingJavaType", "java.math.BigInteger");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigInteger"));
    }

    @Test
    public void applyGeneratesNumber() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUseDoubleNumbers()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Double.class.getName()));
    }

    @Test
    public void applyGeneratesNumberPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloatPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "float");

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloat() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Float");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDoublePrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "double");

        when(config.isUsePrimitives()).thenReturn(false);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDouble() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.lang.Double");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.lang.Double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeBigDecimal() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "number");
        objectNode.put("existingJavaType", "java.math.BigDecimal");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("java.math.BigDecimal"));
    }

    @Test
    public void applyGeneratesBoolean() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Boolean.class.getName()));
    }

    @Test
    public void applyGeneratesBooleanPrimitive() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "boolean");

        when(config.isUsePrimitives()).thenReturn(true);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is("boolean"));
    }

    @Test
    public void applyGeneratesAnyAsObject() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "any");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "null");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "array");

        AbstractJClass mockArrayType = mock(AbstractJClass.class);
        ArrayRule mockArrayRule = mock(ArrayRule.class);
        when(mockArrayRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockArrayType);
        when(ruleFactory.getArrayRule()).thenReturn(mockArrayRule);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is(mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "object");

        JDefinedClass mockObjectType = mock(JDefinedClass.class);
        ObjectRule mockObjectRule = mock(ObjectRule.class);
        when(mockObjectRule.apply("fooBar", objectNode, null, jpackage, null)).thenReturn(mockObjectType);
        when(ruleFactory.getObjectRule()).thenReturn(mockObjectRule);

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result, is(mockObjectType));
    }

    @Test
    public void applyChoosesObjectOnUnrecognizedType() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("type", "unknown");

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));

    }

    @Test
    public void applyDefaultsToTypeAnyObject() throws JCodeModelException {

        JPackage jpackage = new JCodeModel()._package(getClass().getPackage().getName());

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        AbstractJType result = rule.apply("fooBar", objectNode, null, jpackage, null);

        assertThat(result.fullName(), is(Object.class.getName()));
    }

}
