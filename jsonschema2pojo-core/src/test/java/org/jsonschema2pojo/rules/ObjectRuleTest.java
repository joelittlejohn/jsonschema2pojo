/**
 * Copyright © 2015 Matt Francis
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObjectRuleTest {

    private ObjectRule rule;

    @Before
    public void before() {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.isIncludeConstructors()).thenReturn(true);
        when(config.isConstructorsRequiredPropertiesOnly()).thenReturn(true);
        this.rule = new ObjectRule(new RuleFactory(config, new NoopAnnotator(), new SchemaStore()), null);
    }

    @Test
    public void constructorProducedForObjectWithRequiredArray() {
        JCodeModel codeModel = new JCodeModel();
        JPackage jpackage = codeModel._package(getClass().getPackage().getName());

        ObjectMapper mapper = new ObjectMapper();

        ArrayNode requiredNode = mapper.createArrayNode();
        requiredNode.add("requiredProperty1");
        requiredNode.add("requiredProperty2");

        ObjectNode requiredProperty1Node = mapper.createObjectNode();
        requiredProperty1Node.put("type", "string");

        ObjectNode requiredProperty2Node = mapper.createObjectNode();
        requiredProperty2Node.put("type", "string");

        ObjectNode requiredProperty3Node = mapper.createObjectNode();
        requiredProperty3Node.put("type", "string");
        requiredProperty3Node.put("required", "true");

        ObjectNode optionalPropertyNode = mapper.createObjectNode();
        optionalPropertyNode.put("type", "string");

        ObjectNode propertiesNode = mapper.createObjectNode();
        propertiesNode.put("requiredProperty1", requiredProperty1Node);
        propertiesNode.put("requiredProperty2", requiredProperty2Node);
        propertiesNode.put("requiredProperty3", requiredProperty3Node);
        propertiesNode.put("optionalProperty", optionalPropertyNode);

        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("required", requiredNode);
        objectNode.put("properties", propertiesNode);

        JDefinedClass objectType = (JDefinedClass) rule.apply("fooBars", objectNode, jpackage, mock(Schema.class));

        JMethod constructor = null;
        for (Iterator<JMethod> i = objectType.constructors(); i.hasNext(); ) {
            JMethod method = i.next();
            JVar[] params = method.listParams();
            if (params.length > 0) {
                constructor = method;
                break;
            }
        }

        assertThat(constructor, notNullValue());
        assertThat(constructor.params().size(), is(3));
        
        List<String> params = new ArrayList<String>();
        params.add(constructor.params().get(0).name());
        params.add(constructor.params().get(1).name());
        params.add(constructor.params().get(2).name());
        
        assertThat(params, contains("requiredProperty1", "requiredProperty2", "requiredProperty3"));
    }

}
