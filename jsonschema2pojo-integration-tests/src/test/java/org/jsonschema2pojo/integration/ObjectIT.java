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

package org.jsonschema2pojo.integration;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ObjectIT {


    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings("rawtypes")
    public void objectsWithSimpleClassNames() {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/object/objectWithNonUniqueNestedProperty.json",
                "com.example",
                config(
                        "useContextualClassNames", false,
                        "useContextualSubPackages", false
                )
        );

        try {
            Class firstClass = resultsClassLoader.loadClass("com.example.FirstProperty");
            assertEquals("FirstProperty", firstClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("First class has not been generated with name 'FirstProperty'");
        }
        try {
            Class secondClass = resultsClassLoader.loadClass("com.example.FirstProperty_");
            assertEquals("FirstProperty_", secondClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("Second class has not been generated with name 'FirstProperty_'");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void objectsWithContextualClassNames() {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/object/objectWithNonUniqueNestedProperty.json",
                "com.example",
                config(
                        "useContextualClassNames", true,
                        "useContextualSubPackages", false
                )
        );

        try {
            Class firstClass = resultsClassLoader.loadClass("com.example.ObjectWithNonUniqueNestedPropertyFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertyFirstProperty", firstClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("First class has not been generated with name 'ObjectWithNonUniqueNestedPropertyFirstProperty'");
        }
        try {
            Class secondClass = resultsClassLoader.loadClass("com.example.ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty", secondClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("Second class has not been generated with name 'ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty'");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void objectsWithContextualPackageNames() {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/object/objectWithNonUniqueNestedProperty.json",
                "com.example",
                config(
                        "useContextualClassNames", false,
                        "useContextualSubPackages", true
                )
        );

        try {
            Class firstClass = resultsClassLoader.loadClass("com.example.objectwithnonuniquenestedproperty.FirstProperty");
            assertEquals("FirstProperty", firstClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("First class has not been generated with name 'FirstProperty'");
        }
        try {
            Class secondClass = resultsClassLoader.loadClass("com.example.objectwithnonuniquenestedproperty.secondproperty.FirstProperty");
            assertEquals("FirstProperty", secondClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("Second class has not been generated with name 'FirstProperty'");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void objectsWithContextualPackageAndClassNames() {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/object/objectWithNonUniqueNestedProperty.json",
                "com.example",
                config(
                        "useContextualClassNames", true,
                        "useContextualSubPackages", true
                )
        );

        try {
            Class firstClass = resultsClassLoader.loadClass("com.example.objectwithnonuniquenestedproperty.ObjectWithNonUniqueNestedPropertyFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertyFirstProperty", firstClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("First class has not been generated with name 'ObjectWithNonUniqueNestedPropertyFirstProperty'");
        }
        try {
            Class secondClass = resultsClassLoader.loadClass("com.example.objectwithnonuniquenestedproperty.secondproperty.ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty", secondClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("Second class has not been generated with name 'ObjectWithNonUniqueNestedPropertySecondPropertyFirstProperty'");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void objectsWithContextualClassNamesAndDelimiter() {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/object/objectWithNonUniqueNestedProperty.json",
                "com.example",
                config(
                        "useContextualClassNames", true,
                        "contextualClassNameDelimiter", "Sub"
                )
        );

        try {
            Class firstClass = resultsClassLoader.loadClass("com.example.ObjectWithNonUniqueNestedPropertySubFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertySubFirstProperty", firstClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("First class has not been generated with name 'ObjectWithNonUniqueNestedPropertySubFirstProperty'");
        }
        try {
            Class secondClass = resultsClassLoader.loadClass("com.example.ObjectWithNonUniqueNestedPropertySubSecondPropertySubFirstProperty");
            assertEquals("ObjectWithNonUniqueNestedPropertySubSecondPropertySubFirstProperty", secondClass.getSimpleName());
        } catch (ClassNotFoundException e) {
            fail("Second class has not been generated with name 'ObjectWithNonUniqueNestedPropertySubSecondPropertySubFirstProperty'");
        }
    }

}
