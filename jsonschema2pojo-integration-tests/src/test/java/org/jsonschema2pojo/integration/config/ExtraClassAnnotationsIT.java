package org.jsonschema2pojo.integration.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ExtraClassAnnotationsIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void shouldNotAnnotateWithMethodAnnotation() throws ClassNotFoundException {
        Class<SomeOnlyMethodAnnotation> classNotOk = SomeOnlyMethodAnnotation.class;

        Class<?> clazz = schemaRule.generateAndCompile(
                "/schema/default/default.json",
                "com.example",
                config("extraClassAnnotations", new String[]{classNotOk.getName()}))
            .loadClass("com.example.Default");

        assertNull(clazz.getAnnotation(classNotOk));
    }

    @Test
    public void shouldAnnotate() throws ClassNotFoundException {
        Class<SomeOnlyTypeAnnotation> classOk = SomeOnlyTypeAnnotation.class;

        Class<?> clazz = schemaRule.generateAndCompile(
                "/schema/default/default.json",
                "com.example",
                config("extraClassAnnotations", new String[]{classOk.getName()}))
            .loadClass("com.example.Default");

        assertNotNull(clazz.getAnnotation(classOk));
    }

    @Test
    public void shouldAnnotateMultiple() throws ClassNotFoundException {
        Class<SomeOnlyMethodAnnotation> classNotOk = SomeOnlyMethodAnnotation.class;
        Class<SomeOnlyTypeAnnotation> classOk1 = SomeOnlyTypeAnnotation.class;
        Class<SomeTypeAndFieldAnnotation> classOk2 = SomeTypeAndFieldAnnotation.class;

        Class<?> clazz = schemaRule.generateAndCompile(
                "/schema/default/default.json",
                "com.example",
                config("extraClassAnnotations", new String[]{classNotOk.getName(), classOk1.getName(), classOk2.getName()}))
            .loadClass("com.example.Default");

        assertNull(clazz.getAnnotation(classNotOk));
        assertNotNull(clazz.getAnnotation(classOk1));
        assertNotNull(clazz.getAnnotation(classOk2));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SomeOnlyMethodAnnotation {
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SomeOnlyTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    public @interface SomeTypeAndFieldAnnotation {
    }
}
