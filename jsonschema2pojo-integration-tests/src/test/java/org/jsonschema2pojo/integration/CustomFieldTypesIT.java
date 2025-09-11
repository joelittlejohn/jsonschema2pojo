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

package org.jsonschema2pojo.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.jsonschema2pojo.rules.Rule;
import org.apache.tools.ant.util.StreamUtils;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.codemodel.JType;

/**
 * This test shows how the FieldTypeRule can be used to alter the
 * type declaration for fields.
 */
public class CustomFieldTypesIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    public static class NullAsOptionalFieldTypeRule implements Rule<JType, JType> {
        public NullAsOptionalFieldTypeRule(RuleFactory ruleFactory) {
            super();
        }

        @Override
        public JType apply(String nodeName, JsonNode node, JsonNode parent, JType schemaType, Schema schema) {
            Optional<JsonNode> possibleTypeNode = Optional.ofNullable(node.get("type"));

            boolean canBeNull = possibleTypeNode
              .map(typeNode->typeNode.isArray()
                ? StreamSupport.stream(typeNode.spliterator(), false)
                  .anyMatch(n->"null".equals(n.asText()))
                : "null".equals(typeNode.asText())
            ).orElse(false);
            
            if( canBeNull ) {
              return schemaType.owner()
                  .ref(Optional.class)
                  .narrow(schemaType);
            }
            return schemaType;
        }
    }

    public static class ExtendedRuleFactory extends RuleFactory {
        private NullAsOptionalFieldTypeRule fieldTypeRule = new NullAsOptionalFieldTypeRule(this);

        @Override
        public Rule<JType, JType> getFieldTypeRule() {
            return fieldTypeRule;
        }
    }

    @Test
    public void nullFieldsForOptional() throws Exception {
        final String filePath = "/schema/type/types.json";
        ClassLoader resultClassLoader = schemaRule.generateAndCompile(
            filePath, 
            "com.example",
            config(
            "customRuleFactory", ExtendedRuleFactory.class.getName()
            )
        );

        Class<?> rootType = resultClassLoader.loadClass("com.example.Types");
        assertThat(rootType.getDeclaredField("nullableStringProperty").getType(), typeCompatibleWith(Optional.class));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());

        Object value = mapper.readValue("{\"nullableStringProperty\": null}", rootType);

        @SuppressWarnings("unchecked")
        Optional<String> nullableString = (Optional<String>)rootType.getDeclaredMethod("getNullableStringProperty").invoke(value);
        assertThat(nullableString.isPresent(), is(false));
    }
}
