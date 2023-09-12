/**
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

package org.jsonschema2pojo.integration.ref;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class RootRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    @Test(expected = ClassNotFoundException.class)
    public void classIsNotGenerated() throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader relativeRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/rootRefToA.json", "com.example");
        Class<?> rootRefClass = relativeRefsClassLoader.loadClass("com.example.RootRefToA");
    }

    @Test
    public void referenceIsGeneratedSuccessfully() throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader relativeRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/rootRefToA.json", "com.example");
        Class<?> aClass = relativeRefsClassLoader.loadClass("com.example.A");
        assertThat(aClass.getMethod("getPropertyOfA"), notNullValue());
    }

}