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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class IncludeHashCodeAndEqualsIT {

    @RegisterExtension
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @RegisterExtension
    public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();

    private static ClassLoader resultsClassLoader;

    @BeforeAll
    public static void generateAndCompileClass() {
        resultsClassLoader = classSchemaRule.generateAndCompile("/schema/hashCodeAndEquals/types.json", "com.example", config("includeAdditionalProperties", false));
    }

    @Test
    public void beansIncludeHashCodeAndEqualsByDefault() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        // throws NoSuchMethodException if method is not found
        generatedType.getDeclaredMethod("equals", java.lang.Object.class);
        generatedType.getDeclaredMethod("hashCode");

    }

    @Test
    public void beansOmitHashCodeAndEqualsWhenConfigIsSet() throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("includeHashcodeAndEquals", false));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        assertThrows(
                NoSuchMethodException.class,
                () -> generatedType.getDeclaredMethod("equals", java.lang.Object.class),
                ".equals method is present, it should have been omitted");
        assertThrows(
                NoSuchMethodException.class,
                () -> generatedType.getDeclaredMethod("hashCode"),
                ".hashCode method is present, it should have been omitted");
    }

    @Test
    public void objectWithoutFields() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.Empty");
        assertThat(genType.getDeclaredFields().length, is(equalTo(0)));

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertThat("Should not use super.equals()", genType.getDeclaredConstructor().newInstance(), is(equalTo(genType.getDeclaredConstructor().newInstance())));
        assertThat(genType.getDeclaredConstructor().newInstance().hashCode(), is(equalTo(genType.getDeclaredConstructor().newInstance().hashCode())));
    }

    @Test
    public void objectExtendingJavaType() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsJavaType");
        assertThat(genType.getSuperclass(), is(equalTo(Parent.class)));
        assertThat(genType.getDeclaredFields().length, is(equalTo(0)));

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertThat(
                "Should use super.equals() because parent is not Object; parent uses Object.equals()",
                genType.getDeclaredConstructor().newInstance(),
                is(not(equalTo(genType.getDeclaredConstructor().newInstance()))));
    }

    @Test
    public void objectExtendingJavaTypeWithEquals() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsJavaTypeWithEquals");
        assertThat(genType.getSuperclass(), is(equalTo(ParentWithEquals.class)));
        assertThat(genType.getDeclaredFields().length, is(equalTo(0)));

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertThat("Should use super.equals()", genType.getDeclaredConstructor().newInstance(), is(equalTo(genType.getDeclaredConstructor().newInstance())));
        assertThat(genType.getDeclaredConstructor().newInstance().hashCode(), is(equalTo(genType.getDeclaredConstructor().newInstance().hashCode())));
    }

    @Test
    public void objectExtendingFalseObject() throws Exception {

        Class genType = resultsClassLoader.loadClass("com.example.ExtendsFalseObject");
        assertThat(genType.getSuperclass(), is(equalTo(Object.class)));

        genType.getDeclaredMethod("equals", java.lang.Object.class);
        assertThat(
                "Should use super.equals() because parent is not java.lang.Object; parent uses Object.equals()",
                genType.getDeclaredConstructor().newInstance(),
                is(not(equalTo(genType.getDeclaredConstructor().newInstance()))));
    }

    @Test
    public void objectExtendingEmptyParent() throws Exception {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/hashCodeAndEquals/extendsEmpty.json", "com.example");
        Class gen1Type = resultsClassLoader.loadClass("com.example.ExtendsEmptyParent");
        Class gen2Type = resultsClassLoader.loadClass("com.example.ExtendsEmpty");

        gen2Type.getDeclaredMethod("equals", java.lang.Object.class);
        gen2Type.getDeclaredMethod("hashCode");
        assertThat(gen2Type.getDeclaredConstructor().newInstance(), is(equalTo(gen2Type.getDeclaredConstructor().newInstance())));
        assertThat(gen2Type.getDeclaredConstructor().newInstance().hashCode(), is(equalTo(gen2Type.getDeclaredConstructor().newInstance().hashCode())));

        gen1Type.getDeclaredMethod("equals", java.lang.Object.class);
        gen1Type.getDeclaredMethod("hashCode");
        assertThat(gen1Type.getDeclaredConstructor().newInstance(), is(equalTo(gen1Type.getDeclaredConstructor().newInstance())));
        assertThat(gen1Type.getDeclaredConstructor().newInstance().hashCode(), is(equalTo(gen1Type.getDeclaredConstructor().newInstance().hashCode())));
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
            return other instanceof ParentWithEquals;
        }

        @Override
        public int hashCode() {
            return 0;
        }

    }

}
