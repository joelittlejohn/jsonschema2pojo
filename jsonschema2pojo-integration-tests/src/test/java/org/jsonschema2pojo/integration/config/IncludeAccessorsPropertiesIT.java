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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

/**
 * Checks general properties of includeAccessors and different configurations.
 * 
 * @author Christian Trimble
 *
 */
@SuppressWarnings({ "rawtypes" })
@RunWith(Parameterized.class)
public class IncludeAccessorsPropertiesIT {
    public static final String PACKAGE = "com.example";
    public static final String PRIMITIVE_JSON = "/schema/properties/primitiveProperties.json";
    public static final String PRIMITIVE_TYPE = "com.example.PrimitiveProperties";

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config() },
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config("useJodaDates", true) },
            { PRIMITIVE_JSON, PRIMITIVE_TYPE, config("includeAdditionalProperties", false) }
        });
    }

    private String path;
    private String typeName;
    private Map<String, Object> includeAccessorsFalse;
    private Map<String, Object> includeAccessorsTrue;

    public IncludeAccessorsPropertiesIT(String path, String typeName, Map<String, Object> config) {
        this.path = path;
        this.typeName = typeName;
        this.includeAccessorsFalse = configWithIncludeAccessors(config, false);
        this.includeAccessorsTrue = configWithIncludeAccessors(config, true);
    }

    @Test
    public void noGettersOrSettersWhenFalse() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, includeAccessorsFalse);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        for (Method method : generatedType.getDeclaredMethods()) {
            assertThat("getters and setters should not exist", method.getName(), not(anyOf(startsWith("get"), startsWith("set"))));
        }
    }

    @Test
    public void hasGettersOrSettersWhenTrue() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, includeAccessorsTrue);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        List<String> methodNames = new ArrayList<String>();
        for (Method method : generatedType.getDeclaredMethods()) {
            methodNames.add(method.getName());
        }
        assertThat("a getter or setter should be found.", methodNames, hasItem(anyOf(startsWith("get"), startsWith("set"))));
    }

    @Test
    public void onlyHasPublicInstanceFieldsWhenFalse() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, includeAccessorsFalse);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        for (Field field : generatedType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            assertThat("only public instance fields exist", Modifier.isPublic(field.getModifiers()), equalTo(true));
        }
    }

    @Test
    public void noPublicInstanceFieldsWhenTrue() throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, includeAccessorsTrue);
        Class generatedType = resultsClassLoader.loadClass(typeName);

        for (Field field : generatedType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            assertThat("only public instance fields exist", Modifier.isPublic(field.getModifiers()), not(equalTo(true)));
        }
    }

    private static Map<String, Object> configWithIncludeAccessors(Map<String, Object> template, boolean includeAccessors) {
        Map<String, Object> config = new HashMap<String, Object>(template);
        config.put("includeAccessors", includeAccessors);
        return config;
    }
}
