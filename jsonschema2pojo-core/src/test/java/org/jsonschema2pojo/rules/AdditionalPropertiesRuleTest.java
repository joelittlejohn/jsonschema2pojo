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

import static org.junit.Assert.assertEquals;

import org.jsonschema2pojo.Schema;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public class AdditionalPropertiesRuleTest {

    @Test
    public void testSchemaWithNullURI() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = (ObjectNode) mapper
                .readTree("{\"type\":\"integer\",\"maximum\":9}");
        ObjectNode parent = (ObjectNode) mapper
                .readTree("{\"type\":\"object\",\"additionalProperties\":{\"type\":\"integer\",\"maximum\":9}}");
        Schema schema = new Schema(null, parent, null);

        JDefinedClass jclass = new JCodeModel()._class("org.jsonschema2pojo.rules.ExampleClass");
        AdditionalPropertiesRule rule = new AdditionalPropertiesRule(new RuleFactory());

        JDefinedClass result = rule.apply("node", node, parent, jclass, schema);
        JMethod method = result.getMethod("getAdditionalProperties", new JType[0]);
        JClass returnType = (JClass) method.type();
        assertEquals("Map<String,Integer>", returnType.name());
    }

}
