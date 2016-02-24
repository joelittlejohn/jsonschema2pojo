/**
 * Copyright ¬© 2010-2014 Nokia
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

import java.util.Collection;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

import edu.emory.mathcs.backport.java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

@RunWith(Enclosed.class)
public class Jsonschema2PojoRuleTest {

    public static class MethodTests {
        @Rule
        public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().mute();

        @Rule
        public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();

        @Test
        public void sourcesWillCompile() throws ClassNotFoundException {
            ClassLoader resultsClassLoader = rule.generateAndCompile("/schema/default/default.json", "com.example");
            resultsClassLoader.loadClass("com.example.Default");
        }

        @Test
        public void compilationProblemsStdErr() {
          try {
            rule.generateAndCompile("/schema/default/default.json", "com.example", config("customRuleFactory", BrokenRuleFactory.class.getName()));
          } catch( Throwable t ) {}
          assertThat(systemErrRule.getLog(), containsString("return"));
        }

        public static class BrokenRuleFactory extends RuleFactory {
          @Override
          public org.jsonschema2pojo.rules.Rule<JPackage, JType> getObjectRule() {
            final org.jsonschema2pojo.rules.Rule<JPackage, JType> workingRule = super.getObjectRule();

            return new org.jsonschema2pojo.rules.Rule<JPackage, JType>() {
              @Override
              public JType apply(String nodeName, JsonNode node, JPackage generatableType, Schema currentSchema) {
                JType objectType = workingRule.apply(nodeName, node, generatableType, currentSchema);
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

    @RunWith(Parameterized.class)
    public static class ParameterizedTest {
        @Rule
        public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();

        @SuppressWarnings("unchecked")
        @Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            return Arrays.asList(new Object[][] { { "label1" }, { "label2" }, { "../../../" } });
        }

        public ParameterizedTest(String label) {
        }

        @Test
        public void sourcesForLabelsWillCompile() throws ClassNotFoundException {
            ClassLoader resultsClassLoader = rule.generateAndCompile("/schema/default/default.json", "com.example");
            resultsClassLoader.loadClass("com.example.Default");
        }
    }
}
