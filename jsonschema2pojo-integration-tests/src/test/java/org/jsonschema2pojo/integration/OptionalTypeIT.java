package org.jsonschema2pojo.integration;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;

public class OptionalTypeIT {

    @ClassRule
    public static Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class<?> nullableClass;
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void generateAndCompileClass() throws ClassNotFoundException {
        ClassLoader classLoader = schemaRule.generateAndCompile("/schema/optionalType/optionalTypeSchema.json",
                "com.example",
                config("targetVersion", "1.8")
        );
        nullableClass = classLoader.loadClass("com.example.OptionalTypeSchema");
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
        assertThat(getter.getReturnType().getName(), is("java.lang.Double"));
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
        assertThat(numberGetter.invoke(instance), is(123.45));
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
}