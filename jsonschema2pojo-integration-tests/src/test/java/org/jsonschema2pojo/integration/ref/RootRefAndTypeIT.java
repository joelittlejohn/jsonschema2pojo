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

public class RootRefAndTypeIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    @Test(expected = ClassNotFoundException.class)
    public void classIsNotGenerated() throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader relativeRefsClassLoader = classSchemaRule.generateAndCompile("/schema/type/rootTypeWithRef.json", "com.example");
        Class<?> rootRefClass = relativeRefsClassLoader.loadClass("com.example.RootTypeWithRef");
    }

}