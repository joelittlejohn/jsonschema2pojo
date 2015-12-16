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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class IncludeHashCodeAndEqualsIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void beansIncludeHashCodeAndEqualsByDefault() throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        // throws NoSuchMethodException if method is not found
        generatedType.getDeclaredMethod("equals", Object.class);
        generatedType.getDeclaredMethod("hashCode");

    }

    @Test
    public void beansOmitHashCodeAndEqualsWhenConfigIsSet() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("includeHashcodeAndEquals", false));

        Class generatedType = resultsClassLoader.loadClass("com.example.PrimitiveProperties");

        try {
            generatedType.getDeclaredMethod("equals", Object.class);
            fail(".equals method is present, it should have been omitted");
        } catch (NoSuchMethodException e) {
        }

        try {
            generatedType.getDeclaredMethod("hashCode");
            fail(".hashCode method is present, it should have been omitted");
        } catch (NoSuchMethodException e) {
        }
    }

}
