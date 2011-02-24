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

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class ArrayRuleTest {

    private final ArrayRule rule = new ArrayRule(new RuleFactoryImpl(null));

    @Before 
    public void clearSchemaCache() {
        Schema.clearCache();
    }

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

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, createMock(Schema.class));

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

        Schema schema = createMock(Schema.class);
        expect(schema.getId()).andReturn(URI.create("http://example/nonUniqueArray")).anyTimes();
        schema.setJavaTypeIfEmpty(codeModel.DOUBLE);
        replay(schema);

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

        Schema schema = createMock(Schema.class);
        expect(schema.getId()).andReturn(URI.create("http://example/defaultArray")).anyTimes();
        schema.setJavaTypeIfEmpty(codeModel.BOOLEAN);
        replay(schema);

        JClass propertyType = rule.apply("fooBars", propertyNode, jpackage, schema);

        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
    }

}
