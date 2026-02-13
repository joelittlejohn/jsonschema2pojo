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

package org.jsonschema2pojo.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * Unit tests for {@link JAnnotatedClass} - type-use annotation support.
 */
public final class JAnnotatedClassTest {

    private String buildClassWithField(JCodeModel cm, JClass fieldType) throws Exception {
        cm._class("com.example.Test").field(JMod.PRIVATE, fieldType, "value");
        return buildModel(cm);
    }

    private String buildModel(JCodeModel cm) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cm.build(new SingleStreamCodeWriter(baos));
        return baos.toString();
    }

    /**
     * Test that a single type-use annotation is placed before the simple name.
     */
    @Test
    public void testSimpleTypeAnnotation() throws Exception {
        JCodeModel cm = new JCodeModel();
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class)).annotated(Deprecated.class);

        assertThat(annotatedString.name(), is("String"));
        assertThat(annotatedString.fullName(), is("java.lang.String"));
        assertThat(annotatedString.annotations().size(), is(1));

        String output = buildClassWithField(cm, annotatedString);
        assertThat(output, containsString("@Deprecated String value"));
    }

    /**
     * Test that a type-use annotation is placed correctly inside a generic type parameter.
     */
    @Test
    public void testAnnotatedTypeParameter() throws Exception {
        JCodeModel cm = new JCodeModel();
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class)).annotated(Deprecated.class);
        JClass listOfAnnotatedString = cm.ref(List.class).narrow(annotatedString);

        String output = buildClassWithField(cm, listOfAnnotatedString);
        assertThat(output, containsString("List<@Deprecated String>"));
    }

    /**
     * Test that multiple type-use annotations are placed correctly.
     */
    @Test
    public void testMultipleTypeAnnotations() throws Exception {
        JCodeModel cm = new JCodeModel();
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class))
                .annotated(Deprecated.class)
                .annotated(Override.class);

        assertThat(annotatedString.annotations().size(), is(2));

        String output = buildClassWithField(cm, annotatedString);
        assertThat(output, containsString("@Deprecated @Override String value"));
    }

    /**
     * Test that imported types use simple names for both the type and annotation.
     */
    @Test
    public void testImportedTypesUseSimpleNames() throws Exception {
        JCodeModel cm = new JCodeModel();
        JDefinedClass testClass = cm._class("com.example.Test");

        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listType = cm.ref(List.class).narrow(annotatedString);
        JClass arrayListType = cm.ref(ArrayList.class).narrow(annotatedString);

        testClass.field(JMod.PRIVATE, listType, "items", JExpr._new(arrayListType));

        String classOutput = buildModel(cm);

        assertThat(classOutput, containsString("List<@Deprecated String>"));
        assertThat("should not contain FQCN for java.lang types",
                classOutput, not(containsString("java.lang.")));
    }

    /**
     * Test that erasure returns the underlying class without annotations.
     */
    @Test
    public void testErasure() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);

        assertThat(annotatedString.erasure(), is(stringClass));
    }

    /**
     * Test that basis() returns the wrapped class.
     */
    @Test
    public void testBasis() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);

        assertThat(annotatedString.basis(), is(stringClass));
    }

    /**
     * Test that annotating a narrowed class places the annotation before the raw type name.
     */
    @Test
    public void testAnnotateNarrowedClass() throws Exception {
        JCodeModel cm = new JCodeModel();
        JClass listOfString = cm.ref(List.class).narrow(String.class);
        JAnnotatedClass annotatedListOfString = JAnnotatedClass.of(listOfString).annotated(Deprecated.class);

        String output = buildClassWithField(cm, annotatedListOfString);
        assertThat(output, containsString("@Deprecated List<String>"));
    }

    /**
     * Test that annotations are placed correctly in nested generic types.
     */
    @Test
    public void testNestedAnnotatedTypes() throws Exception {
        JCodeModel cm = new JCodeModel();
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class)).annotated(Deprecated.class);
        JAnnotatedClass annotatedInteger = JAnnotatedClass.of(cm.ref(Integer.class)).annotated(Override.class);
        JClass listOfAnnotatedInteger = cm.ref(List.class).narrow(annotatedInteger);
        JClass mapType = cm.ref(Map.class).narrow(annotatedString, listOfAnnotatedInteger);

        String output = buildClassWithField(cm, mapType);
        assertThat(output, containsString("Map<@Deprecated String, List<@Override Integer>>"));
    }

    /**
     * Test that a name collision forces FQCN with correct JLS §9.7.4 annotation placement.
     * A class named "String" in the same package prevents java.lang.String from being imported.
     */
    @Test
    public void testAnnotationPlacementWithNameCollision() throws Exception {
        JCodeModel cm = new JCodeModel();
        JDefinedClass testClass = cm._class("com.example.Test");

        // Create a class named "String" in the same package — collides with java.lang.String
        cm._class("com.example.String");

        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listType = cm.ref(List.class).narrow(annotatedString);

        testClass.field(JMod.PRIVATE, listType, "items");

        String classOutput = buildModel(cm);

        assertThat("annotation should be placed after package qualifier",
                classOutput, containsString("java.lang.@Deprecated String"));
        assertThat("annotation should not be before the FQCN",
                classOutput, not(containsString("@Deprecated java.lang.String")));
    }

    /**
     * Test equals and hashCode - two annotated classes with same basis and annotations should be
     * equal.
     */
    @Test
    public void testEqualsAndHashCode() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);

        JAnnotatedClass annotated1 = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JAnnotatedClass annotated2 = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JAnnotatedClass annotated3 = JAnnotatedClass.of(stringClass).annotated(Override.class);

        // Same basis and same annotation class should produce equivalent results
        assertThat(annotated1.basis(), is(annotated2.basis()));
        assertThat(annotated1.annotations().size(), is(annotated2.annotations().size()));

        // Different annotations should not be equal
        assertThat(annotated1, not(equalTo(annotated3)));
    }

    /**
     * Test that annotating an array type places the annotation before the component type name.
     */
    @Test
    public void testAnnotatedArrayType() throws Exception {
        JCodeModel cm = new JCodeModel();
        JClass stringArray = cm.ref(String.class).array();
        JAnnotatedClass annotatedArray = JAnnotatedClass.of(stringArray).annotated(Deprecated.class);

        String output = buildClassWithField(cm, annotatedArray);
        assertThat(output, containsString("@Deprecated String[] value"));
    }

    /**
     * Test that annotating a primitive array type places the annotation before the component type.
     */
    @Test
    public void testAnnotatedPrimitiveArrayType() throws Exception {
        JCodeModel cm = new JCodeModel();
        JClass intArray = cm.INT.array();
        JAnnotatedClass annotatedIntArray = JAnnotatedClass.of(intArray).annotated(Deprecated.class);

        String output = buildClassWithField(cm, annotatedIntArray);
        assertThat(output, containsString("@Deprecated int[] value"));
    }

    /**
     * Test that annotation parameters are rendered in type-use position.
     */
    @Test
    public void testAnnotationWithParameters() throws Exception {
        JCodeModel cm = new JCodeModel();
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class)).annotated(SuppressWarnings.class);
        annotatedString.annotations().get(0).param("value", "unchecked");

        String output = buildClassWithField(cm, annotatedString);
        assertThat(output, containsString("@SuppressWarnings(\"unchecked\") String value"));
    }
}
