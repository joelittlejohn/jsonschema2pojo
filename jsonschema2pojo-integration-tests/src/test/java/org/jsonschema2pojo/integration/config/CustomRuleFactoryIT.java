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

package org.jsonschema2pojo.integration.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;
import org.joda.time.LocalDate;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.FormatRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertThat;

public class CustomRuleFactoryIT {

    @org.junit.Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void customAnnotatorIsAbleToAddCustomAnnotations() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/format/formattedProperties.json", "com.example",
                config("customRuleFactory", TestRuleFactory.class.getName()));

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.FormattedProperties");

        Method getter = generatedType.getMethod("getStringAsDate");

        Class<?> returnType = getter.getReturnType();
        assertThat(returnType.equals(LocalDate.class), is(true));
    }

    public static class TestRuleFactory extends RuleFactory {

        @Override
        public Rule<JType, JType> getFormatRule() {
            return new FormatRule(this) {
                @Override
                public JType apply(String nodeName, JsonNode node, JsonNode parent, JType baseType, Schema schema) {
                    if (node.asText().equals("date")) {
                        return baseType.owner().ref(LocalDate.class);
                    }

                    return super.apply(nodeName, node, parent, baseType, schema);
                }
            };
        }

    }
}
