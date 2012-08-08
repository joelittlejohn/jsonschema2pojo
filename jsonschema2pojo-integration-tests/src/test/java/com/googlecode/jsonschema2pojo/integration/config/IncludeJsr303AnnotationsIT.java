/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.integration.config;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.Schema;

@SuppressWarnings("rawtypes")
public class IncludeJsr303AnnotationsIT {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();;

    @Before
    public void clearSchemaCache() {
        Schema.clearCache();
    }

    @Test
    public void jsrAnnotationsAreNotIncludedByDefault() throws ClassNotFoundException {
        File outputDirectory = generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr303Annotations", true));

        //assertThat(outputDirectory, not(containsText("javax.validation"));
    }

    @Test
    public void jsr303DecimalMinValidationIsAddedForSchemaRuleMinimum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minimum.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Minimum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minimum", 2.0d);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minimum", 0.9d);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303DecimalMaxValidationIsAddedForSchemaRuleMaximum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/maximum.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Maximum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maximum", 8.9d);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maximum", 10.9d);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minItems.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/maxItems.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItemsAndMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minAndMaxItems.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinAndMaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance1 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1));

        assertNumberOfConstraintViolationsOn(invalidInstance1, is(1));

        Object invalidInstance2 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3, 4, 5));

        assertNumberOfConstraintViolationsOn(invalidInstance2, is(1));

    }

    @Test
    public void jsr303PatternValidationIsAddedForSchemaRulePattern() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/pattern.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Pattern");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "pattern", "abc123");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "pattern", "123abc");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303NotNullValidationIsAddedForSchemaRuleRequired() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "required", "abc");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "required", null);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    private static void assertNumberOfConstraintViolationsOn(Object instance, Matcher<Integer> matcher) {
        Set<ConstraintViolation<Object>> violationsForValidInstance = validator.validate(instance);
        assertThat(violationsForValidInstance.size(), matcher);
    }

    private static Object createInstanceWithPropertyValue(Class type, String propertyName, Object propertyValue) {
        try {
            Object instance = type.newInstance();
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, type);
            propertyDescriptor.getWriteMethod().invoke(instance, propertyValue);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
