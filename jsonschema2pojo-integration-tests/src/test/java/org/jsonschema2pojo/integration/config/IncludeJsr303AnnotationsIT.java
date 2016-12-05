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

import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.junit.Assert.*;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class IncludeJsr303AnnotationsIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();;

    @Test
    public void jsrAnnotationsAreNotIncludedByDefault() throws ClassNotFoundException {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example");

        assertThat(outputDirectory, not(containsText("javax.validation")));
    }

    @Test
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff() throws ClassNotFoundException {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr303Annotations", false));

        assertThat(outputDirectory, not(containsText("javax.validation")));
    }

    @Test
    public void jsr303DecimalMinValidationIsAddedForSchemaRuleMinimum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minimum.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Minimum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minimum", 2.0d);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minimum", 0.9d);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303DecimalMaxValidationIsAddedForSchemaRuleMaximum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maximum.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Maximum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maximum", 8.9d);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maximum", 10.9d);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minItems.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maxItems.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItemsAndMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minAndMaxItems.json", "com.example",
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

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/pattern.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Pattern");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "pattern", "abc123");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "pattern", "123abc");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303NotNullValidationIsAddedForSchemaRuleRequired() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "required", "abc");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "required", null);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinLength() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minLength.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Long enough");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Too short");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxLength() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maxLength.json", "com.example",
                config("includeJsr303Annotations", true));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Short");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Tooooo long");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303ValidAnnotationIsAddedForObject() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validObject.json", "com.example",
                config("includeJsr303Annotations", true));

        Class validObjectType = resultsClassLoader.loadClass("com.example.ValidObject");
        Class objectFieldType = resultsClassLoader.loadClass("com.example.Objectfield");

        Object invalidObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "Too long");
        Object validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", invalidObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(1));

        Object validObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "OK");
        validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", validObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(0));
    }

    @Test
    public void jsr303ValidAnnotationIsAddedForArray() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class objectArrayType = resultsClassLoader.loadClass("com.example.Objectarray");

        List<Object> objectArrayList = new ArrayList<Object>();

        Object objectArrayInstance = createInstanceWithPropertyValue(objectArrayType, "arrayitem", "OK");
        objectArrayList.add(objectArrayInstance);
        Object validArrayInstance = createInstanceWithPropertyValue(validArrayType, "objectarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(0));

        Object invalidObjectArrayInstance = createInstanceWithPropertyValue(objectArrayType, "arrayitem", "Too long");
        objectArrayList.add(invalidObjectArrayInstance);
        validArrayInstance = createInstanceWithPropertyValue(validArrayType, "objectarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(1));
    }

    @Test
    public void jsr303ValidAnnotationIsAddedForArrayWithRef() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class refarrayType = resultsClassLoader.loadClass("com.example.Refarray");

        List<Object> objectArrayList = new ArrayList<Object>();

        Object objectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "OK");
        objectArrayList.add(objectArrayInstance);
        Object validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(0));

        Object invalidObjectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "Too long");
        objectArrayList.add(invalidObjectArrayInstance);
        validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void jsr303AnnotionsValidatedForAdditionalProperties() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validAdditionalProperties.json", "com.example", config("includeJsr303Annotations", true));

        Class parentType = resultsClassLoader.loadClass("com.example.ValidAdditionalProperties");
        Object parent = parentType.newInstance();

        Class subPropertyType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesProperty");
        Object validSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 9.0D);
        Object invalidSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 11.0D);

        Method setter = parentType.getMethod("setAdditionalProperty", String.class, subPropertyType);

        setter.invoke(parent, "maximum", validSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(0));

        setter.invoke(parent, "maximum", invalidSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(1));
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

    private static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }
}
