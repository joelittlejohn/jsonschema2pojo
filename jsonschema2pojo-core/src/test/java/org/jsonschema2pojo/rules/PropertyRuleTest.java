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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Before;
import org.junit.Test;

public class PropertyRuleTest {
    private static final String TARGET_CLASS_NAME = PropertyRuleTest.class.getName() + ".DummyClass";

    private static final String internalFieldName = "internalRequired";
    private static final String targetFieldName = "requiredFoo";

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final PropertyRule rule = new PropertyRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();

        when(config.isIncludeGetters()).thenReturn(true);
        when(config.isUseOptionalForGetters()).thenReturn(true);
    }

    private String getGeneratedMethodTypeName(JDefinedClass jclass) {
        return jclass.getMethod("getRequiredFoo", new JType[] {}).type().name();
    }

    private Schema getMockedSchema(ObjectNode parentNode) {
        Schema schema = mock(Schema.class);
        when(schema.getContent()).thenReturn(parentNode);
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        return schema;
    }

    private JDefinedClass applyRule(ObjectNode propertyNode, ObjectNode parentNode) throws JClassAlreadyExistsException {
        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);
        return rule.apply(targetFieldName, propertyNode, parentNode, jclass, getMockedSchema(parentNode));
    }

    @Test
    public void applyRequiredByTopArray() throws JClassAlreadyExistsException {
        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("required", mapper.createArrayNode().add(internalFieldName));
        propertyNode.set("properties", mapper.createObjectNode().set(internalFieldName, mapper.createObjectNode()));

        ObjectNode parentNode = mapper.createObjectNode();
        parentNode.set("required", mapper.createArrayNode().add(targetFieldName));
        parentNode.set("properties", mapper.createObjectNode().set(targetFieldName, propertyNode));

        JDefinedClass jclass = applyRule(propertyNode, parentNode);

        assertThat(jclass, notNullValue());
        assertThat(getGeneratedMethodTypeName(jclass), is("RequiredFoo"));
    }

    @Test
    public void applyNotRequiredByTopArray() throws JClassAlreadyExistsException {
        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("required", mapper.createArrayNode().add(internalFieldName));
        propertyNode.set("properties", mapper.createObjectNode().set(internalFieldName, mapper.createObjectNode()));

        ObjectNode parentNode = mapper.createObjectNode();
        parentNode.set("properties", mapper.createObjectNode().set(targetFieldName, propertyNode));

        JDefinedClass jclass = applyRule(propertyNode, parentNode);

        assertThat(jclass, notNullValue());
        assertThat(getGeneratedMethodTypeName(jclass), is("Optional<RequiredFoo>"));
    }

    @Test
    public void applyRequiredByFlag() throws JClassAlreadyExistsException {
        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("required", BooleanNode.TRUE);
        propertyNode.set("properties", mapper.createObjectNode().set(internalFieldName, mapper.createObjectNode()));

        ObjectNode parentNode = mapper.createObjectNode();
        parentNode.set("properties", mapper.createObjectNode().set(targetFieldName, propertyNode));

        JDefinedClass jclass = applyRule(propertyNode, parentNode);

        assertThat(jclass, notNullValue());
        assertThat(getGeneratedMethodTypeName(jclass), is("RequiredFoo"));
    }

    @Test
    public void applyNotRequiredByFlag() throws JClassAlreadyExistsException {
        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("properties", mapper.createObjectNode().set(internalFieldName, mapper.createObjectNode()));

        ObjectNode parentNode = mapper.createObjectNode();
        parentNode.set("properties", mapper.createObjectNode().set(targetFieldName, propertyNode));

        JDefinedClass jclass = applyRule(propertyNode, parentNode);

        assertThat(jclass, notNullValue());
        assertThat(getGeneratedMethodTypeName(jclass), is("Optional<RequiredFoo>"));
    }
}
