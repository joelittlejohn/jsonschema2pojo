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

import org.jsonschema2pojo.integration.util.FileSearchMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.ClassWithSizeAnnotation;

@SuppressWarnings("rawtypes")
@ParameterizedClass
@ValueSource(booleans = { true, false })
public class IncludeJsr303AnnotationsIT {

    private final boolean useJakartaValidation;
    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static final javax.validation.Validator javaxValidator = javax.validation.Validation.byProvider(ApacheValidationProvider.class)
            .configure()
            .buildValidatorFactory()
            .getValidator();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public IncludeJsr303AnnotationsIT(boolean useJakartaValidation) {
        this.useJakartaValidation = useJakartaValidation;
    }

    @Test
    public void jsrAnnotationsAreNotIncludedByDefault() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @Test
    public void jsrAnnotationsAreNotIncludedWhenSwitchedOff() {
        File outputDirectory = schemaRule.generate("/schema/jsr303/all.json", "com.example",
                config("includeJsr303Annotations", false, "useJakartaValidation", useJakartaValidation));

        final String validationPackageName = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        assertThat(outputDirectory, not(containsText(validationPackageName)));
    }

    @Test
    public void jsr303DecimalMinValidationIsAddedForSchemaRuleMinimum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minimum.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Minimum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minimum", 2);
        setInstancePropertyValue(validInstance, "minimumNotConstrained", 1.5);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minimum", 0);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303DecimalMaxValidationIsAddedForSchemaRuleMaximum() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maximum.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Maximum");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maximum", 8);
        setInstancePropertyValue(validInstance, "maximumNotConstrained", 10.6);

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maximum", 10);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3, 4, 5, 6));
        setInstancePropertyValue(validInstance, "minItemsNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maxItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3));
        setInstancePropertyValue(validInstance, "maxItemsNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxItems", asList(1, 2, 3, 4, 5, 6));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItemsAndMaxItems() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minAndMaxItems.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinAndMaxItems");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance1 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", Collections.singletonList(1));

        assertNumberOfConstraintViolationsOn(invalidInstance1, is(1));

        Object invalidInstance2 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", asList(1, 2, 3, 4, 5));

        assertNumberOfConstraintViolationsOn(invalidInstance2, is(1));

    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinItemsAndMaxItemsOnExistingType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minAndMaxItemsExistingType.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinAndMaxItemsExistingType");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", new java.util.LinkedList<>(asList("a", "b", "c")));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance1 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", new java.util.LinkedList<>(Collections.singletonList("a")));

        assertNumberOfConstraintViolationsOn(invalidInstance1, is(1));

        Object invalidInstance2 = createInstanceWithPropertyValue(generatedType, "minAndMaxItems", new java.util.LinkedList<>(asList("a", "b", "c", "d", "e")));

        assertNumberOfConstraintViolationsOn(invalidInstance2, is(1));

    }

    @Test
    public void jsr303EmailValidationIsAddedForFormatEmailSchemaRule() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/email.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Email");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "email", "user@example.com");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "email", "aaa");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303PatternValidationIsAddedForSchemaRulePattern() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/pattern.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Pattern");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "pattern", "abc123");
        setInstancePropertyValue(validInstance, "patternNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "pattern", "123abc");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303NotNullValidationIsAddedForSchemaRuleRequired() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/required.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Required");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "required", "abc");

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "required", null);

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMinLength() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/minLength.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MinLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Long enough");
        setInstancePropertyValue(validInstance, "minLengthNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "minLength", "Too short");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303SizeValidationIsAddedForSchemaRuleMaxLength() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/maxLength.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.MaxLength");

        Object validInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Short");
        setInstancePropertyValue(validInstance, "maxLengthNotApplicable", UUID.randomUUID());

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "maxLength", "Tooooo long");

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));
    }

    @Test
    public void jsr303DigitsValidationIsAddedForSchemaRuleDigits() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/digits.json", "com.example",
                config("includeJsr303Annotations", true, "useBigDecimals", true, "useJakartaValidation", useJakartaValidation));

        Class generatedType = resultsClassLoader.loadClass("com.example.Digits");

        // positive value
        Object validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("12345.1234567890"));
        setInstancePropertyValue(validInstance, "digitsNotApplicable", Collections.singletonList("12345.12345678901"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        // negative value
        validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("-12345.0123456789"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        // zero value
        validInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("0.0"));

        assertNumberOfConstraintViolationsOn(validInstance, is(0));

        // too many integer digits
        Object invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("123456.0123456789"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

        // too many fractional digits
        invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("12345.12345678901"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

        // too many integer & fractional digits
        invalidInstance = createInstanceWithPropertyValue(generatedType, "decimal", new BigDecimal("123456.12345678901"));

        assertNumberOfConstraintViolationsOn(invalidInstance, is(1));

    }

    @Test
    public void jsr303ValidAnnotationIsAddedForObject() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validObject.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class<?> validObjectType = resultsClassLoader.loadClass("com.example.ValidObject");
        Class<?> objectFieldType = resultsClassLoader.loadClass("com.example.Objectfield");

        Object invalidObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "Too long");
        Object validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", invalidObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(1));

        Object validObjectFieldInstance = createInstanceWithPropertyValue(objectFieldType, "childprimitivefield", "OK");
        validObjectInstance = createInstanceWithPropertyValue(validObjectType, "objectfield", validObjectFieldInstance);

        assertNumberOfConstraintViolationsOn(validObjectInstance, is(0));

        final var expectedAnnotationClass = getValidAnnotationClass();
        assertThat(
                "@Valid should not be on the field, but on the item type",
                validObjectType.getDeclaredField("objectTypeField").isAnnotationPresent(expectedAnnotationClass),
                is(true));
    }

    @Test
    public void jsr303ValidAnnotationIsAddedForArray() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class objectArrayType = resultsClassLoader.loadClass("com.example.Objectarray");

        List<Object> objectArrayList = new ArrayList<>();

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
    public void jsr303ValidAnnotationIsAddedForArrayWithRef() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Class refarrayType = resultsClassLoader.loadClass("com.example.Product");

        List<Object> objectArrayList = new ArrayList<>();

        Object objectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "OK");
        objectArrayList.add(objectArrayInstance);
        Object validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(0));

        Object invalidObjectArrayInstance = createInstanceWithPropertyValue(refarrayType, "arrayitem", "Too long");
        objectArrayList.add(invalidObjectArrayInstance);
        validArrayInstance = createInstanceWithPropertyValue(validArrayType, "refarray", objectArrayList);

        assertNumberOfConstraintViolationsOn(validArrayInstance, is(1));
    }

    @Test
    public void jsr303ValidAnnotationIsOnItemTypeNotField() throws ClassNotFoundException, NoSuchFieldException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> validArrayType = resultsClassLoader.loadClass("com.example.ValidArray");
        Field objectArrayField = validArrayType.getDeclaredField("objectarray");

        // The @Valid annotation should be on the item type (e.g., List<@Valid Item>), not on the field
        assertThat("@Valid should not be on the field, but on the item type",
                objectArrayField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // Verify the @Valid annotation IS present on the type parameter (item type)
        AnnotatedType annotatedType = objectArrayField.getAnnotatedType();
        assertThat("Field type should be an AnnotatedParameterizedType",
                annotatedType, is(instanceOf(AnnotatedParameterizedType.class)));

        AnnotatedParameterizedType parameterizedType =
                (AnnotatedParameterizedType) annotatedType;
        AnnotatedType[] typeArguments = parameterizedType.getAnnotatedActualTypeArguments();

        assertThat("Should have one type argument", typeArguments.length, is(1));

        // Check that the type argument (item type) has @Valid
        Annotation[] itemTypeAnnotations = typeArguments[0].getAnnotations();
        assertThat("Item type should have @Valid annotation", itemTypeAnnotations.length, is(1));
        assertThat("@Valid annotation should be on the item type parameter",
                itemTypeAnnotations[0].annotationType(), is(expectedValidAnnotation));
    }

    @Test
    public void jsr303AnnotationsValidatedForAdditionalProperties() throws ReflectiveOperationException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/jsr303/validAdditionalProperties.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class<?> parentType = resultsClassLoader.loadClass("com.example.ValidAdditionalProperties");
        Object parent = parentType.getDeclaredConstructor().newInstance();

        Class<?> subPropertyType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesProperty");
        Object validSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 9);
        Object invalidSubPropertyInstance = createInstanceWithPropertyValue(subPropertyType, "maximum", 11);

        Method setter = parentType.getMethod("setAdditionalProperty", String.class, subPropertyType);

        setter.invoke(parent, "maximum", validSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(0));

        setter.invoke(parent, "maximum", invalidSubPropertyInstance);
        assertNumberOfConstraintViolationsOn(parent, is(1));

        // Verify that @Valid is on the map value type parameter, not on the field itself
        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Field additionalPropertiesField = parentType.getDeclaredField("additionalProperties");

        assertThat("@Valid should not be on the field, but on the value type parameter",
               additionalPropertiesField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        AnnotatedType annotatedType = additionalPropertiesField.getAnnotatedType();
        assertThat("Field type should be an AnnotatedParameterizedType",
                annotatedType, is(instanceOf(AnnotatedParameterizedType.class)));

        AnnotatedParameterizedType parameterizedType =
                (AnnotatedParameterizedType) annotatedType;
        AnnotatedType[] typeArguments = parameterizedType.getAnnotatedActualTypeArguments();

        assertThat("Should have two type arguments (Map<String, ValueType>)", typeArguments.length, is(2));

        assertThat("Key type (String) should have no annotations", typeArguments[0].getAnnotations().length, is(0));

        Annotation[] valueTypeAnnotations = typeArguments[1].getAnnotations();
        assertThat("Value type should have exactly one annotation", valueTypeAnnotations.length, is(1));
        assertThat("@Valid annotation should be on the value type parameter",
                valueTypeAnnotations[0].annotationType(), is(expectedValidAnnotation));
    }

    @Test
    public void jsr303AnnotationsWithAdditionalPropertiesTrue() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/jsr303/validAdditionalPropertiesTrue.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesTrue");
        Field additionalPropertiesField = generatedType.getDeclaredField("additionalProperties");

        // @Valid should not be on the field
        assertThat("@Valid should not be on the field",
                additionalPropertiesField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // @Valid should be on the Object value type parameter
        AnnotatedParameterizedType mapType =
                (AnnotatedParameterizedType) additionalPropertiesField.getAnnotatedType();
        AnnotatedType[] mapTypeArgs = mapType.getAnnotatedActualTypeArguments();
        assertThat("Map should have two type arguments", mapTypeArgs.length, is(2));
        assertThat("Key type should have no annotations", mapTypeArgs[0].getAnnotations().length, is(0));

        Annotation[] valueTypeAnnotations = mapTypeArgs[1].getAnnotations();
        assertThat("Value type should have exactly one annotation", valueTypeAnnotations.length, is(1));
        assertThat("@Valid annotation should be on the value type parameter",
                valueTypeAnnotations[0].annotationType(), is(expectedValidAnnotation));
    }

    @Test
    public void jsr303ValidAnnotationOnClassWithBuilders() throws Exception {
        schemaRule.generate(
                "/schema/jsr303/validWithBuilders", "com.example",
                config("includeJsr303Annotations", true,
                        "useJakartaValidation", useJakartaValidation,
                        "generateBuilders", true));
        ClassLoader resultsClassLoader = schemaRule.compile();

        Class<?> parentType = resultsClassLoader.loadClass("com.example.Animal");
        Class<?> childType = resultsClassLoader.loadClass("com.example.Dog");

        assertThat("Dog should extend Animal", childType.getSuperclass(), is(parentType));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> tagType = resultsClassLoader.loadClass("com.example.Tag");
        Method withTag = childType.getMethod("withTag", tagType);
        AnnotatedType paramType = withTag.getAnnotatedParameterTypes()[0];
        assertThat("withTag parameter type should have @Valid", paramType.getAnnotations().length, is(1));
        assertThat("@Valid should be on the withTag parameter type", paramType.getAnnotations()[0].annotationType(), is(expectedValidAnnotation));
    }

    @Test
    public void jsr303ValidAnnotationOnSelfRef() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/selfRefs.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> selfRefsType = resultsClassLoader.loadClass("com.example.SelfRefs");

        // Direct self-ref should have @Valid on the field
        Field childOfSelf = selfRefsType.getDeclaredField("childOfSelf");
        assertThat("@Valid should be on self-ref field", childOfSelf.getAnnotation(expectedValidAnnotation), is(not(nullValue())));
    }

    @Test
    public void jsr303ValidAnnotationOnRef() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/refsToA.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> refsToAType = resultsClassLoader.loadClass("com.example.RefsToA");

        // Direct $ref property should have @Valid on the field
        Field aField = refsToAType.getDeclaredField("a");
        assertThat("@Valid should be on the $ref field", aField.getAnnotation(expectedValidAnnotation), is(not(nullValue())));

        // Array of $ref should have @Valid on the item type parameter, not on the field
        Field arrayOfAField = refsToAType.getDeclaredField("arrayOfA");
        assertThat("@Valid should not be on the array field", arrayOfAField.getAnnotation(expectedValidAnnotation), is(nullValue()));
        AnnotatedParameterizedType listType = (AnnotatedParameterizedType) arrayOfAField.getAnnotatedType();
        AnnotatedType[] listTypeArgs = listType.getAnnotatedActualTypeArguments();
        assertThat("Item type should have @Valid", listTypeArgs[0].getAnnotations().length, is(1));
        assertThat("@Valid should be on the item type", listTypeArgs[0].getAnnotations()[0].annotationType(), is(expectedValidAnnotation));

        // additionalProperties $ref should have @Valid on the map value type parameter
        Field additionalPropertiesField = refsToAType.getDeclaredField("additionalProperties");
        assertThat("@Valid should not be on the map field", additionalPropertiesField.getAnnotation(expectedValidAnnotation), is(nullValue()));
        AnnotatedParameterizedType mapType = (AnnotatedParameterizedType) additionalPropertiesField.getAnnotatedType();
        AnnotatedType[] mapTypeArgs = mapType.getAnnotatedActualTypeArguments();
        assertThat("Map value type should have @Valid", mapTypeArgs[1].getAnnotations().length, is(1));
        assertThat("@Valid should be on the map value type", mapTypeArgs[1].getAnnotations()[0].annotationType(), is(expectedValidAnnotation));
    }

    @Test
    public void jsr303ValidAnnotationOnAdditionalPropertiesWithNestedArrays() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/jsr303/validAdditionalPropertiesWithNestedArrays.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesWithNestedArrays");
        Field additionalPropertiesField = generatedType.getDeclaredField("additionalProperties");

        // @Valid should not be on the field itself (Map is a container)
        assertThat(additionalPropertiesField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // Container types are not annotated — only the innermost non-container type has @Valid
        String source = Files.readString(
                schemaRule.generated("com/example/ValidAdditionalPropertiesWithNestedArrays.java").toPath());
        assertThat(source, containsString(
                "Map<String, List<List<List<List<@Valid ValidAdditionalPropertiesWithNestedArraysProperty>>>>>"));

        // Verify cascading validation works through nested containers
        Class<?> itemType = resultsClassLoader.loadClass(
                "com.example.ValidAdditionalPropertiesWithNestedArraysProperty");

        Object validItem = createInstanceWithPropertyValue(itemType, "name", "OK");
        Object invalidItem = createInstanceWithPropertyValue(itemType, "name", "Too long");

        Object parent = generatedType.getDeclaredConstructor().newInstance();
        Method setter = generatedType.getMethod("setAdditionalProperty", String.class, List.class);

        setter.invoke(parent, "test", List.of(List.of(List.of(List.of(validItem)))));
        assertNumberOfConstraintViolationsOn(parent, is(0));

        setter.invoke(parent, "test", List.of(List.of(List.of(List.of(invalidItem)))));
        assertNumberOfConstraintViolationsOn(parent, is(1));
    }

    @Test
    public void jsr303ValidAnnotationOnAdditionalPropertiesWithItemTypeArray() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/jsr303/validAdditionalPropertiesWithItemTypeArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.ValidAdditionalPropertiesWithItemTypeArray");
        Field additionalPropertiesField = generatedType.getDeclaredField("additionalProperties");

        // @Valid should not be on the field itself (Map is a container)
        assertThat("@Valid should not be on the field",
                additionalPropertiesField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // Value type is List<@Valid String> — the List itself is a container and has no @Valid
        AnnotatedParameterizedType mapType =
                (AnnotatedParameterizedType) additionalPropertiesField.getAnnotatedType();
        AnnotatedType[] mapTypeArgs = mapType.getAnnotatedActualTypeArguments();
        assertThat("Map should have two type arguments", mapTypeArgs.length, is(2));

        assertThat("Key type should have no annotations", mapTypeArgs[0].getAnnotations().length, is(0));
        assertThat("Value type (List) should have no annotations (containers are not annotated)",
                mapTypeArgs[1].getAnnotations().length, is(0));
    }

    @Test
    public void jsr303ValidAnnotationsOnExistingJavaTypeWithScalar() throws Exception {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile(
                "/schema/jsr303/validExistingJavaType.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        final Class<? extends Annotation> expectedValidAnnotation = getValidAnnotationClass();
        Class<?> generatedType = resultsClassLoader.loadClass("com.example.ValidExistingJavaType");

        // typedMap: no @Valid (container types are not annotated, existingJavaType items don't go through the pipeline)
        Field typedMapField = generatedType.getDeclaredField("typedMap");
        assertThat(typedMapField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // rawMap: no @Valid (container types are not annotated)
        Field rawMapField = generatedType.getDeclaredField("rawMap");
        assertThat(rawMapField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // typedList: no @Valid (container types are not annotated)
        Field typedListField = generatedType.getDeclaredField("typedList");
        assertThat(typedListField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // rawList: no @Valid (container types are not annotated)
        Field rawListField = generatedType.getDeclaredField("rawList");
        assertThat(rawListField.getAnnotation(expectedValidAnnotation), is(nullValue()));

        // standardType: @Valid on field (not a collection)
        Field standardTypeField = generatedType.getDeclaredField("standardType");
        assertThat(standardTypeField.getAnnotation(expectedValidAnnotation), is(not(nullValue())));
    }

    @Test
    public void jsr303ValidAnnotationUsesSimpleNamesWhenImported() throws Exception {
        schemaRule.generateAndCompile("/schema/jsr303/validArray.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        String source = Files.readString(schemaRule.generated("com/example/ValidArray.java").toPath());

        assertThat("Should use simple name @Valid for inline object types",
                source, containsString("List<@Valid Primitivearray>"));
        assertThat("Should use simple name @Valid for generated types",
                source, containsString("List<@Valid Objectarray>"));
        assertThat("Should use simple name @Valid for $ref types",
                source, containsString("List<@Valid Product>"));
    }

    @Test
    public void jsr303ValidAnnotationUsesCorrectPlacementForQualifiedTypes() throws Exception {
        schemaRule.generateAndCompile("/schema/jsr303/validArrayWithCollision.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        String source = Files.readString(
                schemaRule.generated("com/example/ValidArrayWithCollision.java").toPath());

        assertThat("Should use correct JLS §9.7.4 placement: java.lang.@Valid Object",
                source, containsString("java.lang.@Valid Object"));
        assertThat("Should not use incorrect placement: @Valid java.lang.Object",
                source, not(containsString("@Valid java.lang.Object")));
    }

    @Test
    public void jsr303ValidAnnotationNotAppliedToScalarTypes() throws Exception {
        schemaRule.generateAndCompile("/schema/jsr303/validScalarTypes.json", "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        String source = Files.readString(schemaRule.generated("com/example/ValidScalarTypes.java").toPath());

        assertThat("@Valid should not appear anywhere for scalar-only types",
                source, not(containsString("@Valid")));
    }

    @Test
    public void jsr303AnnotationsValidatedForExistingJavaType() throws ReflectiveOperationException {
        ClassLoader classLoader = schemaRule.generateAndCompile(
                "/schema/jsr303/existingJavaTypeProperties.json",
                "com.example",
                config("includeJsr303Annotations", true, "useJakartaValidation", useJakartaValidation));

        Class<?> clazz = classLoader.loadClass("com.example.ExistingJavaTypeProperties");
        Object instance = clazz.getDeclaredConstructor().newInstance();

        assertNumberOfConstraintViolationsOn(instance, is(0));

        instance = createInstanceWithPropertyValue(
                clazz,
                "mapOfStringOptionalListOfExistingClasses",
                Map.of("a", Optional.of(List.of(new ClassWithSizeAnnotation()))));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(
                clazz,
                "listOfOptionalStringExistingClassMaps",
                List.of(Optional.of(Map.of("a", new ClassWithSizeAnnotation()))));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(
                clazz,
                "listOfOptionalStringObjectMaps",
                List.of(Optional.of(Map.of("a", new ClassWithSizeAnnotation()))));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "optionalOfExistingClass", Optional.of(new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "mapOfStringExistingJavaClasses", Map.of("a", new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "listOfObjects", List.of(new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "mapOfStringObject", Map.of("a", new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "primitiveLong", 1L);
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "rawOptional", Optional.of(new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(0));

        instance = createInstanceWithPropertyValue(clazz, "rawMap", Map.of("a", new ClassWithSizeAnnotation()));
        assertNumberOfConstraintViolationsOn(instance, is(0));

        instance = createInstanceWithPropertyValue(
                clazz,
                "existingTypeAsArrayItem",
                List.of(Map.of("a", new ClassWithSizeAnnotation())));
        assertNumberOfConstraintViolationsOn(instance, is(1));

        instance = createInstanceWithPropertyValue(clazz, "itemOfExistingClass", new ClassWithSizeAnnotation());
        assertNumberOfConstraintViolationsOn(instance, is(1));

        // Note: at present arrays are generated as single item, once arrays are generated correctly below checks should be adjusted
        instance = createInstanceWithPropertyValue(clazz, "arrayOfExistingClasses", new ClassWithSizeAnnotation());
        assertNumberOfConstraintViolationsOn(instance, is(1));

        // Validation does not cascade through multidimensional arrays at present
        instance = createInstanceWithPropertyValue(clazz, "multiDimensionalArrayOfExistingClasses", new ClassWithSizeAnnotation());
        assertNumberOfConstraintViolationsOn(instance, is(1));
    }

    private void assertNumberOfConstraintViolationsOn(Object instance, Matcher<Integer> matcher) {
        final Set<?> violationsForValidInstance = useJakartaValidation ? validator.validate(instance) : javaxValidator.validate(instance);
        final String validatorName = useJakartaValidation ? "jakarta/hibernate validator" : "javax/bval validator";
        assertThat("Violations (" + validatorName + "): " + violationsForValidInstance.toString(), violationsForValidInstance.size(), matcher);
    }

    private static Object createInstanceWithPropertyValue(Class<?> type, String propertyName, Object propertyValue) {
        try {
            Object instance = type.getDeclaredConstructor().newInstance();
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

    private Class<? extends Annotation> getValidAnnotationClass() {
        return useJakartaValidation ? jakarta.validation.Valid.class : javax.validation.Valid.class;
    }

}
