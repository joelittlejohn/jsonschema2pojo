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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.SchemaMapperImpl;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public class ArrayRuleTest {

    private static final String TARGET_CLASS_NAME = ArrayRuleTest.class.getName() + ".DummyClass";

    private ArrayRule rule = new ArrayRule(new SchemaMapperImpl());

    @Test
    public void arrayWithUniqueItemsProducesSet() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "integer");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.put("uniqueItems", true);
        propertyNode.put("items", itemsNode);

        JClass propertyType = rule.apply("fooBars", propertyNode, jclass);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(Set.class)));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Integer.class.getName()));
    }

    @Test
    public void arrayWithNonUniqueItemsProducesList() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "number");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.put("uniqueItems", false);
        propertyNode.put("items", itemsNode);

        JClass propertyType = rule.apply("fooBars", propertyNode, jclass);

        assertThat(propertyType, notNullValue());
        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
        assertThat(propertyType.getTypeParameters().get(0).fullName(), is(Double.class.getName()));
    }

    @Test
    public void arrayDefaultsToNonUnique() throws JClassAlreadyExistsException {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jclass = codeModel._class(TARGET_CLASS_NAME);

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode itemsNode = mapper.createObjectNode();
        itemsNode.put("type", "boolean");

        ObjectNode propertyNode = mapper.createObjectNode();
        propertyNode.put("uniqueItems", false);
        propertyNode.put("items", itemsNode);

        JClass propertyType = rule.apply("fooBars", propertyNode, jclass);

        assertThat(propertyType.erasure(), is(codeModel.ref(List.class)));
    }

}
