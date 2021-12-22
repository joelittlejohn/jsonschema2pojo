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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class IncludeHashCodeAndEqualsIT extends Jsonschema2PojoTestBase {

    private static ClassLoader resultsClassLoader;

    @BeforeEach
    public void generateAndCompileClass() {
        resultsClassLoader = generateAndCompile("/schema/hashCodeAndEquals/types.json", "com.example", config("includeAdditionalProperties", false));
    }

    @Test
    public void beansIncludeHashCodeAndEqualsByDefault() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        // throws NoSuchMethodException if method is not found
        generatedType.getDeclaredMethod("equals", java.lang.Object.class);
        generatedType.getDeclaredMethod("hashCode");

    }

    @Test
    public void beansOmitHashCodeAndEqualsWhenConfigIsSet() throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("includeHashcodeAndEquals", false));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThrows(NoSuchMethodException.class, () ->
                        generatedType.getDeclaredMethod("equals", java.lang.Object.class),
                ".equals method is present, it should have been omitted");
        assertThrows(NoSuchMethodException.class, () ->
                        generatedType.getDeclaredMethod("hashCode"),
                ".hashCode method is present, it should have been omitted");
    }

    @Test
    public void objectWithoutFields() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.Empty");
        assertEquals(genType.getDeclaredFields().length, 0);

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertEquals(genType.newInstance(), genType.newInstance(), "Should not use super.equals()");
        assertEquals(genType.newInstance().hashCode(), genType.newInstance().hashCode());
    }

    @Test
    public void objectExtendingJavaType() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsJavaType");
        assertEquals(genType.getSuperclass(), Parent.class);
        assertEquals(genType.getDeclaredFields().length, 0);

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertNotEquals(genType.newInstance(), genType.newInstance(), "Should use super.equals() because parent is not Object; parent uses Object.equals()");
    }

    @Test
    public void objectExtendingJavaTypeWithEquals() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsJavaTypeWithEquals");
        assertEquals(genType.getSuperclass(), ParentWithEquals.class);
        assertEquals(genType.getDeclaredFields().length, 0);

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertEquals(genType.newInstance(), genType.newInstance(), "Should use super.equals()");
        assertEquals(genType.newInstance().hashCode(), genType.newInstance().hashCode());
    }

    @Test
    public void objectExtendingFalseObject() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsFalseObject");
        assertEquals(genType.getSuperclass(), Object.class);

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertNotEquals( genType.newInstance(), genType.newInstance(), "Should use super.equals() because parent is not java.lang.Object; parent uses Object.equals()");
    }

    @Test
    public void objectExtendingEmptyParent() throws Exception {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/hashCodeAndEquals/extendsEmpty.json", "com.example");
        Class gen1Type = resultsClassLoader.loadClass("com.example.ExtendsEmptyParent");
        Class gen2Type = resultsClassLoader.loadClass("com.example.ExtendsEmpty");

        gen2Type.getDeclaredMethod("equals", java.lang.Object.class);
        gen2Type.getDeclaredMethod("hashCode");
        assertEquals(gen2Type.newInstance(), gen2Type.newInstance());
        assertEquals(gen2Type.newInstance().hashCode(), gen2Type.newInstance().hashCode());

        gen1Type.getDeclaredMethod("equals", java.lang.Object.class);
        gen1Type.getDeclaredMethod("hashCode");
        assertEquals(gen1Type.newInstance(), gen1Type.newInstance());
        assertEquals(gen1Type.newInstance().hashCode(), gen1Type.newInstance().hashCode());
    }

    public static class Object extends java.lang.Object {
    }

    public static class Parent {
    }

    public static class ParentWithEquals {

        @Override
        public boolean equals(java.lang.Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof ParentWithEquals) == false) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

    }

}
