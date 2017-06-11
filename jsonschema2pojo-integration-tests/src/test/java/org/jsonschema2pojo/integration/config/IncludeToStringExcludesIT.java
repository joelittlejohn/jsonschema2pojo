/**
 * Copyright © 2010-2017 Nokia
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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class IncludeToStringExcludesIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void testConfig(Map<String,Object> config, String expectedResultTemplate) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config);

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        // throws NoSuchMethodException if method is not found
        Method toString = generatedType.getDeclaredMethod("toString");
        try {
            Object primitiveProperties = generatedType.newInstance();
            Object result = toString.invoke(primitiveProperties);
            assertEquals(String.format(expectedResultTemplate, Integer.toHexString(System.identityHashCode(primitiveProperties))), result);
        } catch (Exception e) {
            fail("Unable to invoke toString method: "+ e.getMessage());
        }
    }

    @Test
    public void beansIncludeAllToStringPropertiesByDefaultCL() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config(),
                "com.example.PrimitiveProperties@%s[a=<null>,b=<null>,c=<null>,additionalProperties={}]");
    }

    @Test
    public void beansIncludeAllToStringPropertiesByDefaultCL3() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config("useCommonsLang3", true),
                "com.example.PrimitiveProperties@%s[a=<null>,b=<null>,c=<null>,additionalProperties={}]");
    }

    @Test
    public void beansOmitToStringPropertiesWhenConfigIsSetCL() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config("toStringExcludes", new String[] {"b","c"}),
                "com.example.PrimitiveProperties@%s[a=<null>,additionalProperties={}]");
    }

    @Test
    public void beansOmitToStringPropertiesWhenConfigIsSetCL3() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        testConfig(config("useCommonsLang3", true,"toStringExcludes", new String[] {"b","c"}),
                "com.example.PrimitiveProperties@%s[a=<null>,additionalProperties={}]");
    }

}
