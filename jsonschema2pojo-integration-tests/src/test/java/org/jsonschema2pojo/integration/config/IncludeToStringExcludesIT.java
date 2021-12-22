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

import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IncludeToStringExcludesIT extends Jsonschema2PojoTestBase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void testConfig(Map<String, Object> config, String expectedResultTemplate) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config);

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        // throws NoSuchMethodException if method is not found
        Method toString = generatedType.getDeclaredMethod("toString");
        assertDoesNotThrow(
                () -> {
                    Object primitiveProperties = generatedType.newInstance();
                    Object result = toString.invoke(primitiveProperties);
                    assertEquals(String.format(expectedResultTemplate, Integer.toHexString(System.identityHashCode(primitiveProperties))), result);
                },
                "Unable to invoke toString method");
    }

    @Test
    public void beansIncludeAllToStringPropertiesByDefault() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config(),
                "com.example.PrimitiveProperties@%s[a=<null>,b=<null>,c=<null>,additionalProperties={}]");
    }

    @Test
    public void beansOmitToStringProperties() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config("toStringExcludes", new String[]{"b", "c"}),
                "com.example.PrimitiveProperties@%s[a=<null>,additionalProperties={}]");
    }

}
