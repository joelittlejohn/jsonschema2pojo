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

package org.jsonschema2pojo.integration.util;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class Jsonschema2PojoRuleTest {

    @Nested
    class MethodTests {

        @RegisterExtension
        public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();

        @Test
        public void sourcesWillCompile() throws ClassNotFoundException {
            ClassLoader resultsClassLoader = rule.generateAndCompile("/schema/default/default.json", "com.example");
            resultsClassLoader.loadClass("com.example.Default");
        }

        @Test
        public void compilationProblemsStdErr() {
            rule.captureDiagnostics();
            assertThrows(
                    AssertionError.class,
                    () -> rule.generateAndCompile("/schema/default/default.json", "com.example", config("customRuleFactory", BrokenRuleFactory.class.getName())));
            assertThat(rule.getDiagnostics(), hasSize(1));
            assertThat(rule.getDiagnostics().get(0).toString(), containsString("error: missing return statement"));
        }

    }

    @Nested
    @ParameterizedClass(name = "{0}")
    @ValueSource(strings = { "label1", "label2", "../../../" })
    class ParameterizedTest {

        @RegisterExtension
        public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();

        public ParameterizedTest(String label) {
        }

        @Test
        public void sourcesForLabelsWillCompile() throws ClassNotFoundException {
            ClassLoader resultsClassLoader = rule.generateAndCompile("/schema/default/default.json", "com.example");
            resultsClassLoader.loadClass("com.example.Default");
        }
    }

    public static class BrokenRuleFactory extends RuleFactory {
        @Override
        public Rule<JPackage, JType> getObjectRule() {
            final Rule<JPackage, JType> workingRule = super.getObjectRule();

            return new Rule<JPackage, JType>() {
                @Override
                public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage generatableType, Schema currentSchema) {
                    JType objectType = workingRule.apply(nodeName, node, null, generatableType, currentSchema);
                    if( objectType instanceof JDefinedClass ) {
                        JDefinedClass jclass = (JDefinedClass)objectType;
                        jclass.method(JMod.PUBLIC, jclass.owner().BOOLEAN, "brokenMethod").body();
                    }
                    return objectType;
                }
            };
        }
    }

}
