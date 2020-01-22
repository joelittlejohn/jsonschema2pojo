/**
 * Copyright © 2010-2017 Nokia
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

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class ArrayRuleTest {

    private final GenerationConfig config = mock(GenerationConfig.class);
    private final ArrayRule rule = new ArrayRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()));

    @Test
    public void arrayWithUniqueItemsProducesSet() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "integer");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.TRUE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.deriveChildSchema(any())).thenReturn(schema);

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(Set.class)));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Integer.class.getName()));
    }

    @Test
    public void arrayWithUniqueItemsAndCustomSetTypeProducesCustomSet() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "integer");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.TRUE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        when(config.getSetType()).thenReturn(scala.collection.immutable.Set.class.getName());

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure().name(), is(codeModel.ref(scala.collection.immutable.Set.class).name()));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Integer.class.getName()));
    }

    @Test
    public void arrayWithNonUniqueItemsProducesList() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "number");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.FALSE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/nonUniqueArray"));
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Double.class.getName()));
    }

    @Test
    public void arrayWithNonUniqueItemsAndCustomListTypeProducesCustomList() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "number");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.FALSE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/nonUniqueArray"));
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        when(config.isUseDoubleNumbers()).thenReturn(true);
        when(config.getListType()).thenReturn(scala.collection.immutable.List.class.getName());

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure().name(), is(codeModel.ref(scala.collection.immutable.List.class).name()));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Double.class.getName()));
    }

    @Test
    public void arrayOfPrimitivesProducesCollectionOfWrapperTypes() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "number");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.FALSE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/nonUniqueArray"));
        when(schema.deriveChildSchema(any())).thenReturn(schema);
        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Double.class.getName()));
    }

    @Test
    public void arrayDefaultsToNonUnique() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "boolean");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.set("uniqueItems", BooleanNode.FALSE);
        propertyNode.set("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/defaultArray"));
        when(schema.deriveChildSchema(any())).thenReturn(schema);

        JClass propertyType = rule.apply("fooBars", propertyNode, null, jpackage, schema);

        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
    }

}
