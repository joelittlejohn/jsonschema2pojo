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

package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;

public class NullableTypeIT {

    @ClassRule
    public static Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class<?> nullableClass;
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/nullableType/nullableTypeSchema.json",
                "com.example",
                config("useJakartaValidation", true,
                        "includeJsr303Annotations", true,
                        "useBigDecimals", true)
        );

        nullableClass = classLoader.loadClass("com.example.NullableTypeSchema");

        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configOverride(nullableClass).setInclude(
                JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS));
    }

    @Test
    public void nullableStringVer1IsStringType() throws Exception {
        Method getter = nullableClass.getMethod("getNullableStringVer1");
        assertThat(getter.getReturnType().getName(), is("java.lang.String"));
    }

    @Test
    public void nullableStringVer2IsStringType() throws Exception {
        Method getter = nullableClass.getMethod("getNullableStringVer2");
        assertThat(getter.getReturnType().getName(), is("java.lang.String"));
    }

    @Test
    public void nullableNumberIsDoubleType() throws Exception {
        Method getter = nullableClass.getMethod("getNullableNumber");
        assertThat(getter.getReturnType().getName(), is("java.math.BigDecimal"));
    }

    @Test
    public void nullableBooleanIsBooleanType() throws Exception {
        Method getter = nullableClass.getMethod("getNullableBoolean");
        assertThat(getter.getReturnType().getName(), is("java.lang.Boolean"));
    }

    @Test
    public void unionTypeIsObjectType() throws Exception {
        Method getter = nullableClass.getMethod("getUnionTypeField");
        assertThat(getter.getReturnType().getName(), is("java.lang.Object"));
    }

    @Test
    public void canDeserializeWithNullValues() throws Exception {
        String json = "{\"nullableStringVer1\": null, \"nullableStringVer2\": null, \"nullableNumber\": null, \"nullableBoolean\": null}";
        Object instance = objectMapper.readValue(json, nullableClass);

        Method stringGetter1 = nullableClass.getMethod("getNullableStringVer1");
        Method stringGetter2 = nullableClass.getMethod("getNullableStringVer2");
        Method numberGetter = nullableClass.getMethod("getNullableNumber");
        Method booleanGetter = nullableClass.getMethod("getNullableBoolean");

        assertThat(stringGetter1.invoke(instance), is(nullValue()));
        assertThat(stringGetter2.invoke(instance), is(nullValue()));
        assertThat(numberGetter.invoke(instance), is(nullValue()));
        assertThat(booleanGetter.invoke(instance), is(nullValue()));
    }

    @Test
    public void canDeserializeWithValues() throws Exception {
        String json = "{\"nullableStringVer1\": \"test\", \"nullableStringVer2\": \"test2\", \"nullableNumber\": 123.45, \"nullableBoolean\": true}";
        Object instance = objectMapper.readValue(json, nullableClass);

        Method stringGetter1 = nullableClass.getMethod("getNullableStringVer1");
        Method stringGetter2 = nullableClass.getMethod("getNullableStringVer2");
        Method numberGetter = nullableClass.getMethod("getNullableNumber");
        Method booleanGetter = nullableClass.getMethod("getNullableBoolean");

        assertThat(stringGetter1.invoke(instance), is("test"));
        assertThat(stringGetter2.invoke(instance), is("test2"));
        assertThat(numberGetter.invoke(instance), is(new BigDecimal("123.45")));
        assertThat(booleanGetter.invoke(instance), is(true));
    }

    @Test
    public void canSerializeWithNullValues() throws Exception {
        Object instance = nullableClass.newInstance();

        // Then serialize it back to JSON
        String outputJson = objectMapper.writeValueAsString(instance);
        JsonNode jsonNode = objectMapper.readTree(outputJson);

        // Verify the properties exist and are null
        assertThat(jsonNode.has("nullableStringVer1"), is(true));
        assertThat(jsonNode.get("nullableStringVer1").isNull(), is(true));

        assertThat(jsonNode.has("nullableStringVer2"), is(true));
        assertThat(jsonNode.get("nullableStringVer2").isNull(), is(true));

        assertThat(jsonNode.has("nullableNumber"), is(true));
        assertThat(jsonNode.get("nullableNumber").isNull(), is(true));

        assertThat(jsonNode.has("nullableBoolean"), is(true));
        assertThat(jsonNode.get("nullableBoolean").isNull(), is(true));
    }

    @Test
    public void nullableNumberHasValidationAnnotations() throws Exception {
        Field optionalString = nullableClass.getDeclaredField("nullableStringVer1");
        Field nullableNumberField = nullableClass.getDeclaredField("nullableNumber");

        Pattern pattern = optionalString.getAnnotation(Pattern.class);
        assertNotNull("Nullable string should have pattern", pattern);
        assertThat(pattern.regexp(), is("^[a-zA-Z0-9]+$"));

        DecimalMin decimalMinAnnotation = nullableNumberField.getAnnotation(DecimalMin.class);
        assertNotNull("nullableNumber field should have @DecimalMin annotation", decimalMinAnnotation);
        assertEquals("10", decimalMinAnnotation.value());

        DecimalMax decimalMax = nullableNumberField.getAnnotation(DecimalMax.class);
        assertNotNull("nullableNumber field should have @DecimalMax annotation", decimalMinAnnotation);
        assertEquals("12", decimalMax.value());
    }

    @Test
    public void optionalEnumIsEnumType() throws Exception {
        Method getter = nullableClass.getMethod("getOptionalEnum");
        Class<?> enumType = getter.getReturnType();

        assertTrue("optionalEnum should be an enum type", enumType.isEnum());

        Object[] enumConstants = enumType.getEnumConstants();
        assertThat("Enum should have 2 constants", enumConstants.length, is(2));

        Set<String> enumValues = new HashSet<>();
        for (Object enumConstant : enumConstants) {
            enumValues.add(enumConstant.toString());
        }

        assertThat("Enum should contain OPTION_1 and OPTION_2",
                enumValues, containsInAnyOrder("OPTION_1", "OPTION_2"));
    }

    @Test
    public void requiredNullableFieldsHaveCorrectAnnotations() throws Exception {
        // Fields that should not have @NotNull despite being required
        Field nullableStringVer1 = nullableClass.getDeclaredField("nullableStringVer1");
        Field nullableBoolean = nullableClass.getDeclaredField("nullableBoolean");

        // Check Jakarta Validation annotations
        assertNull("Required but nullable field should not have @NotNull annotation",
                nullableStringVer1.getAnnotation(NotNull.class));
        assertNull("Required but nullable field should not have @NotNull annotation",
                nullableBoolean.getAnnotation(NotNull.class));

        // Check JSR-305 annotations if they're enabled
        if (nullableStringVer1.isAnnotationPresent(javax.annotation.Nullable.class)) {
            assertNotNull("Required but nullable field should have @Nullable annotation",
                    nullableStringVer1.getAnnotation(javax.annotation.Nullable.class));
            assertNotNull("Required but nullable field should have @Nullable annotation",
                    nullableBoolean.getAnnotation(javax.annotation.Nullable.class));

            assertNull("Required but nullable field should not have @Nonnull annotation",
                    nullableStringVer1.getAnnotation(javax.annotation.Nonnull.class));
            assertNull("Required but nullable field should not have @Nonnull annotation",
                    nullableBoolean.getAnnotation(javax.annotation.Nonnull.class));
        }

        // Check non-required nullable fields
        Field nullableNumber = nullableClass.getDeclaredField("nullableNumber");
        Field nullableStringVer2 = nullableClass.getDeclaredField("nullableStringVer2");

        assertNull("Optional nullable field should not have @NotNull annotation",
                nullableNumber.getAnnotation(NotNull.class));
        assertNull("Optional nullable field should not have @NotNull annotation",
                nullableStringVer2.getAnnotation(NotNull.class));

        // Test getters for annotation presence
        Method stringGetter1 = nullableClass.getMethod("getNullableStringVer1");
        Method booleanGetter = nullableClass.getMethod("getNullableBoolean");

        assertNull("Getter for required but nullable field should not have @NotNull annotation",
                stringGetter1.getAnnotation(NotNull.class));
        assertNull("Getter for required but nullable field should not have @NotNull annotation",
                booleanGetter.getAnnotation(NotNull.class));
    }

}