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

import static java.util.Arrays.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.jsonschema2pojo.integration.util.Compiler.getJavaVersion;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("rawtypes")
public class IncludeJsr303AnnotationsIT extends Jsonschema2PojoTestBase {

    private static final javax.validation.Validator javaxValidator = javax.validation.Validation.byProvider(ApacheValidationProvider.class)
            .configure()
            .buildValidatorFactory()
            .getValidator();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrAnnotationsAreNotIncludedByDefault(boolean useJakartaValidation) {
        File outputDirectory = generate("/schema/jsr303/all.json", "com.example",
                config("useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff(boolean useJakartaValidation) {
        File outputDirectory = generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr303Annotations", false, "useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303DecimalMinValidationIsAddedForSchemaRuleMinimum(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minimum.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Minimum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minimum", 2);
        setInstancePropertyValue(validInstance, "minimumNotConstrained", 1.5);

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minimum", 0);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303DecimalMaxValidationIsAddedForSchemaRuleMaximum(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/maximum.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Maximum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maximum", 8);
        setInstancePropertyValue(validInstance, "maximumNotConstrained", 10.6);

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maximum", 10);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

    }

    // Doesn't work correctly for JDK 9+ with useJakartaValidation = false
    @ParameterizedTest
    @MethodSource("parameters")
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItems(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3, 4, 5, 6));
        setInstancePropertyValue(validInstance, "minItemsNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

    }

    // Doesn't work correctly for JDK 9+ with useJakartaValidation = false
    @ParameterizedTest
    @MethodSource("parameters")
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxItems(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/maxItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3));
        setInstancePropertyValue(validInstance, "maxItemsNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);
    }

    // Doesn't work correctly for JDK 9+ with useJakartaValidation = false
    @ParameterizedTest
    @MethodSource("parameters")
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItemsAndMaxItems(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minAndMaxItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinAndMaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance1 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", Collections.singletonList(1));

        assertNumberOfConstraintViolationsOn(invalidInstance1, is(1), useJakartaValidation);

        Object invalidInstance2 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3, 4, 5));

        assertNumberOfConstraintViolationsOn(invalidInstance2, is(1), useJakartaValidation);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303PatternValidationIsAddedForSchemaRulePattern(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/pattern.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Pattern");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "pattern", "abc123");
        setInstancePropertyValue(validInstance, "patternNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "pattern", "123abc");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303NotNullValidationIsAddedForSchemaRuleRequired(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "required", "abc");

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "required", null);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303SizeValidationIsAddedForSchemaRuleMinLength(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/minLength.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Long enough");
        setInstancePropertyValue(validInstance, "minLengthNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Too short");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxLength(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/maxLength.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Short");
        setInstancePropertyValue(validInstance, "maxLengthNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Tooooo long");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);
    }

    // Doesn't work correctly for JDK 9+ with useJakartaValidation = false
    @ParameterizedTest
    @MethodSource("parameters")
    public void jsr303DigitsValidationIsAddedForSchemaRuleDigits(boolean useJakartaValidation) throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/digits.json", "com.example",
                config("includeJsr303Annotations", true, "useBigDecimals", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Digits");

        // positive value
        Object validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("12345.1234567890"));
        setInstancePropertyValue(validInstance, "digitsNotApplicable", Collections.singletonList("12345.12345678901"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        // negative value
        validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("-12345.0123456789"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        // zero value
        validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("0.0"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0), useJakartaValidation);

        // too many integer digits
        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("123456.0123456789"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

        // too many fractional digits
        invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("12345.12345678901"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

        // too many integer & fractional digits
        invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("123456.12345678901"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1), useJakartaValidation);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303ValidAnnotationIsAddedForObject(boolean useJakartaValidation) throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/validObject.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class validObjectType = resultsClassLoader.loadClass("com.example.ValidObject");
        Class objectFieldType = resultsClassLoader.loadClass("com.example.Objectfield");

        Object invalidObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "Too long");
        Object validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", invalidObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(1), useJakartaValidation);

        Object validObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "OK");
        validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", validObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(0), useJakartaValidation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303ValidAnnotationIsAddedForArray(boolean useJakartaValidation) throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class objectArrayType = resultsClassLoader.loadClass("com.example.Objectarray");

        List<Object> objectArrayList = new ArrayList<>();

        Object objectArrayInstance = createInstanceWithPropertyValue(objectArrayType, "arrayitem", "OK");
        objectArrayList.add(objectArrayInstance);
        Object validArrayInstance = createInstanceWithPropertyValue(validArrayType, "objectarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(0), useJakartaValidation);

        Object invalidObjectArrayInstance = createInstanceWithPropertyValue(objectArrayType, "arrayitem", "Too long");
        objectArrayList.add(invalidObjectArrayInstance);
        validArrayInstance = createInstanceWithPropertyValue(validArrayType, "objectarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(1), useJakartaValidation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jsr303ValidAnnotationIsAddedForArrayWithRef(boolean useJakartaValidation) throws ClassNotFoundException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class refarrayType = resultsClassLoader.loadClass("com.example.Product");

        List<Object> objectArrayList = new ArrayList<>();

        Object objectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "OK");
        objectArrayList.add(objectArrayInstance);
        Object validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(0), useJakartaValidation);

        Object invalidObjectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "Too long");
        objectArrayList.add(invalidObjectArrayInstance);
        validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(1), useJakartaValidation);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void jar303AnnotationsValidatedForAdditionalProperties(boolean useJakartaValidation) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/jsr303/validAdditionalProperties.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class parentType = resultsClassLoader.loadClass("com.example.ValidAdditionalProperties");
        Object parent = parentType.newInstance();

        Class subPropertyType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesProperty");
        Object validSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 9);
        Object invalidSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 11);

        Method setter = parentType.getMethod("setAdditionalProperty", String.class, subPropertyType);

        setter.invoke(parent, "maximum", validSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(0), useJakartaValidation);

        setter.invoke(parent, "maximum", invalidSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(1), useJakartaValidation);
    }

    private void assertNumberOfConstraintViolationsOn(Object instance, Matcher<Integer> matcher, boolean useJakartaValidation) {
        final Set<?> violationsForValidInstance = useJakartaValidation ? validator.validate(instance) : javaxValidator.validate(instance);
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

    private static void setInstancePropertyValue(Object instance, String propertyName, Object propertyValue) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, instance.getClass());
            propertyDescriptor.getWriteMethod().invoke(instance, propertyValue);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Matcher<File> containsText(String searchText) {
        return new FileSearchMatcher(searchText);
    }


    public static Stream<Arguments> parameters() {
        if (getJavaVersion() == 8) {
            return Stream.of(Arguments.of(false), Arguments.of(true));
        } else {
            return Stream.of(Arguments.of(true));
        }
    }
}
