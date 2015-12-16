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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.emory.mathcs.backport.java.util.Arrays;

@RunWith(Enclosed.class)
public class Jsonschema2PojoRuleTest {

    public static class MethodTests {
        @Rule
        public Jsonschema2PojoRule rule = new Jsonschema2PojoRule();

        @Test
        public void sourcesWillCompile() throws ClassNotFoundException {
            ClassLoader resultsClassLoader = rule.generateAndCompile("/schema/default/default.json", "com.example");
            resultsClassLoader.loadClass("com.example.Default");
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
