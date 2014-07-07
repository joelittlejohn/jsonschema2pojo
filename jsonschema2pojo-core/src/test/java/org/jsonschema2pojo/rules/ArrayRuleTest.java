/**
 * Copyright © 2010-2014 Nokia
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

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
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
        propertyNode.put("uniqueItems", true);
        propertyNode.put("items", itemsNode);

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, mock(Schema.class));

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(Set.class)));
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
        propertyNode.put("uniqueItems", false);
        propertyNode.put("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/nonUniqueArray"));
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, schema);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
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
        propertyNode.put("uniqueItems", false);
        propertyNode.put("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/nonUniqueArray"));
        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, schema);

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
        propertyNode.put("uniqueItems", false);
        propertyNode.put("items", itemsNode);

        Schema schema = mock(Schema.class);
        when(schema.getId()).thenReturn(URI.create("http://example/defaultArray"));

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, schema);

        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
    }

}
