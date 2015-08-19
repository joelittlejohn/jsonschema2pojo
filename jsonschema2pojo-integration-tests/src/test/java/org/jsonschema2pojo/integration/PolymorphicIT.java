/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo.integration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Test;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author JAshe
 */
public class PolymorphicIT {
    
    @Test
    @SuppressWarnings("rawtypes")
    public void extendsWithPolymorphicDeserialization() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/polymorphic/childArraySchema1.json", "com.example");

        Class subtype = resultsClassLoader.loadClass("com.example.ChildArraySchema1");
        Class supertype = subtype.getSuperclass();

        assertNotNull(supertype.getAnnotation(JsonTypeInfo.class));
        assertNotNull(supertype.getAnnotation(JsonSubTypes.class));

    }
}
