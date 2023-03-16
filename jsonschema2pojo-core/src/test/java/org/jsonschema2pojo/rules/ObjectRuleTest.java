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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public class ObjectRuleTest {

    protected final GenerationConfig generationConfig = mock(GenerationConfig.class);
    protected final RuleFactory ruleFactory = mock(RuleFactory.class);
    protected final Annotator annotator = mock(Annotator.class);
    protected final NameHelper nameHelper = mock(NameHelper.class);
    protected final RuleLogger ruleLogger = mock(RuleLogger.class);
    protected final ParcelableHelper parcelableHelper = mock(ParcelableHelper.class);
    protected final ReflectionHelper reflectionHelper = mock(ReflectionHelper.class);
    protected final AdditionalPropertiesRule additionalPropertiesRule = mock(AdditionalPropertiesRule.class);
    protected final DynamicPropertiesRule dynamicPropertiesRule = mock(DynamicPropertiesRule.class);
    protected final PropertiesRule propertiesRule = mock(PropertiesRule.class);
    protected final Schema schema = mock(Schema.class);
    protected final JCodeModel codeModel = new JCodeModel();
    protected final JType superType = codeModel.ref(Object.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final ObjectRule underTest = new ObjectRule(ruleFactory, parcelableHelper, reflectionHelper);

    @Before
    public void setUp() {
        lenient().when(ruleFactory.getAdditionalPropertiesRule()).thenReturn(additionalPropertiesRule);
        lenient().when(ruleFactory.getDynamicPropertiesRule()).thenReturn(dynamicPropertiesRule);
        lenient().when(ruleFactory.getAnnotator()).thenReturn(annotator);
        lenient().when(ruleFactory.getNameHelper()).thenReturn(nameHelper);
        lenient().when(ruleFactory.getReflectionHelper()).thenReturn(reflectionHelper);
        lenient().when(ruleFactory.getPropertiesRule()).thenReturn(propertiesRule);
        lenient().when(ruleFactory.getGenerationConfig()).thenReturn(generationConfig);
        lenient().when(ruleFactory.getLogger()).thenReturn(ruleLogger);
    }

    @Test
    public void testSimpleObject() {
        JPackage jpackage = codeModel._package("org.jsonschema2pojo.test");

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("type", "object");
        ObjectNode propertiesNode = objectNode.putObject("properties");
        ObjectNode additionalPropertiesNode = objectNode.putObject("additionalProperties");

        propertiesNode.putObject("prop").put("type", "string");

        when(reflectionHelper.getSuperType("Parent", objectNode, jpackage, schema)).thenReturn(superType);
        when(reflectionHelper.isFinal(superType)).thenReturn(false);
        when(nameHelper.getUniqueClassName("Parent", objectNode, jpackage)).thenReturn("Parent");

        JType result = underTest.apply("Parent", objectNode, null, jpackage, schema);

        assertThat(codeModel.countArtifacts(), equalTo(1));
        assertThat(result.binaryName(), is("org.jsonschema2pojo.test.Parent"));
        assertThat(result, instanceOf(JDefinedClass.class));

        final JDefinedClass definedClass = (JDefinedClass) result;
        assertThat(definedClass.fields().values(), empty());

        verify(propertiesRule).apply(eq("Parent"), eq(propertiesNode), eq(objectNode), any(), eq(schema));
        verify(additionalPropertiesRule)
                .apply(eq("Parent"), eq(additionalPropertiesNode), eq(objectNode), any(), eq(schema));
        verify(dynamicPropertiesRule)
                .apply(eq("Parent"), eq(propertiesNode), eq(objectNode), any(), eq(schema));
    }

    @Test
    public void testSimpleObjectAsNested() throws Exception {
        JPackage jpackage = codeModel._package("org.jsonschema2pojo.test");
        JDefinedClass jClass = jpackage._class("Parent");

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("type", "object");
        ObjectNode propertiesNode = objectNode.putObject("properties");
        ObjectNode additionalPropertiesNode = objectNode.putObject("additionalProperties");

        propertiesNode.putObject("prop").put("type", "string");

        when(reflectionHelper.getSuperType("Child", objectNode, jpackage, schema)).thenReturn(superType);
        when(reflectionHelper.isFinal(superType)).thenReturn(false);
        when(nameHelper.getUniqueClassName("Child", objectNode, jClass)).thenReturn("Child");

        JType result = underTest.apply("Child", objectNode, null, jClass, schema);

        assertThat(codeModel.countArtifacts(), equalTo(1));
        assertThat(result.binaryName(), is("org.jsonschema2pojo.test.Parent$Child"));
        assertThat(result, instanceOf(JDefinedClass.class));

        final JDefinedClass definedClass = (JDefinedClass) result;
        assertThat(definedClass.fields().values(), empty());

        verify(propertiesRule).apply(eq("Child"), eq(propertiesNode), eq(objectNode), any(), eq(schema));
        verify(additionalPropertiesRule)
                .apply(eq("Child"), eq(additionalPropertiesNode), eq(objectNode), any(), eq(schema));
        verify(dynamicPropertiesRule)
                .apply(eq("Child"), eq(propertiesNode), eq(objectNode), any(), eq(schema));
    }

}
