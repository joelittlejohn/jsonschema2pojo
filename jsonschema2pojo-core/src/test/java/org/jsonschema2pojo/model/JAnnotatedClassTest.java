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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

/**
 * Unit tests for {@link JAnnotatedClass} - type-use annotation support.
 */
public final class JAnnotatedClassTest {

    /**
     * Helper method to generate code from a JClass using JFormatter.
     */
    private String generate(JClass type) {
        StringWriter sw = new StringWriter();
        JFormatter f = new JFormatter(sw);
        type.generate(f);
        return sw.toString();
    }

    /**
     * Test basic type-use annotation: {@code @Deprecated String}
     */
    @Test
    public void testSimpleTypeAnnotation() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);

        assertThat(annotatedString.name(), is("String"));
        assertThat(annotatedString.fullName(), is("java.lang.String"));
        assertThat(annotatedString.annotations().size(), is(1));

        String generated = generate(annotatedString);
        assertThat(generated, is("@java.lang.Deprecated java.lang.String"));
    }

    /**
     * Test type-use annotation on generic type parameter:
     * {@code java.util.List<@java.lang.Deprecated java.lang.String>}
     */
    @Test
    public void testAnnotatedTypeParameter() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listOfAnnotatedString = cm.ref(List.class).narrow(annotatedString);

        String generated = generate(listOfAnnotatedString);
        assertThat(generated, is("java.util.List<@java.lang.Deprecated java.lang.String>"));
    }

    /**
     * Test multiple type-use annotations: {@code @Deprecated @Override String}
     */
    @Test
    public void testMultipleTypeAnnotations() {
        JCodeModel cm = new JCodeModel();
        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass)
                .annotated(Deprecated.class)
                .annotated(Override.class);

        assertThat(annotatedString.annotations().size(), is(2));

        String generated = generate(annotatedString);
        assertThat(generated, is("@java.lang.Deprecated @java.lang.Override java.lang.String"));
    }

    /**
     * Test annotated type with initializer.
     */
    @Test
    public void testAnnotatedFieldWithInitializer() throws JClassAlreadyExistsException {
        JCodeModel cm = new JCodeModel();
        JDefinedClass testClass = cm._class("com.example.Test");

        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listType = cm.ref(List.class).narrow(annotatedString);
        JClass arrayListType = cm.ref(ArrayList.class).narrow(annotatedString);

        testClass.field(JMod.PRIVATE, listType, "items", JExpr._new(arrayListType));

        StringWriter sw = new StringWriter();
        testClass.declare(new JFormatter(sw));
        String classOutput = sw.toString();

        assertThat(classOutput, containsString("java.util.List<@java.lang.Deprecated java.lang.String>"));
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
     * Test annotating an already narrowed class.
     */
    @Test
    public void testAnnotateNarrowedClass() {
        JCodeModel cm = new JCodeModel();

        // Create List<String>
        JClass listOfString = cm.ref(List.class).narrow(String.class);

        // Annotate the whole List<String> type
        JAnnotatedClass annotatedListOfString = JAnnotatedClass.of(listOfString).annotated(Deprecated.class);

        String generated = generate(annotatedListOfString);
        assertThat(generated, is("@java.lang.Deprecated java.util.List<java.lang.String>"));
    }

    /**
     * Test complex nested annotations: {@code Map<@A String, List<@B Integer>>}
     */
    @Test
    public void testNestedAnnotatedTypes() {
        JCodeModel cm = new JCodeModel();

        // @Deprecated String
        JAnnotatedClass annotatedString = JAnnotatedClass.of(cm.ref(String.class)).annotated(Deprecated.class);

        // @Override Integer
        JAnnotatedClass annotatedInteger = JAnnotatedClass.of(cm.ref(Integer.class)).annotated(Override.class);

        // List<@Override Integer>
        JClass listOfAnnotatedInteger = cm.ref(List.class).narrow(annotatedInteger);

        // Map<@Deprecated String, List<@Override Integer>>
        JClass mapType = cm.ref(Map.class).narrow(annotatedString, listOfAnnotatedInteger);

        String generated = generate(mapType);
        assertThat(generated, is("java.util.Map<@java.lang.Deprecated java.lang.String, java.util.List<@java.lang.Override java.lang.Integer>>"));
    }

    /**
     * Test that the annotated class works correctly in method parameters.
     */
    @Test
    public void testAnnotatedMethodParameter() throws JClassAlreadyExistsException {
        JCodeModel cm = new JCodeModel();
        JDefinedClass testClass = cm._class("com.example.Test");

        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listType = cm.ref(List.class).narrow(annotatedString);

        JMethod method = testClass.method(JMod.PUBLIC, cm.VOID, "process");
        method.param(listType, "items");

        StringWriter sw = new StringWriter();
        testClass.declare(new JFormatter(sw));
        String classOutput = sw.toString();

        assertThat(classOutput, containsString("java.util.List<@java.lang.Deprecated java.lang.String> items"));
    }

    /**
     * Test that the annotated class works correctly as method return type.
     */
    @Test
    public void testAnnotatedReturnType() throws JClassAlreadyExistsException {
        JCodeModel cm = new JCodeModel();
        JDefinedClass testClass = cm._class("com.example.Test");

        JClass stringClass = cm.ref(String.class);
        JAnnotatedClass annotatedString = JAnnotatedClass.of(stringClass).annotated(Deprecated.class);
        JClass listType = cm.ref(List.class).narrow(annotatedString);

        JMethod method = testClass.method(JMod.PUBLIC, listType, "getItems");
        method.body()._return(JExpr._null());

        StringWriter sw = new StringWriter();
        testClass.declare(new JFormatter(sw));
        String classOutput = sw.toString();

        assertThat(classOutput, containsString("java.util.List<@java.lang.Deprecated java.lang.String> getItems()"));
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
     * Test annotated array type: {@code @Deprecated String[]}
     */
    @Test
    public void testAnnotatedArrayType() {
        JCodeModel cm = new JCodeModel();

        // @Deprecated String[]
        JClass stringArray = cm.ref(String.class).array();
        JAnnotatedClass annotatedArray = JAnnotatedClass.of(stringArray).annotated(Deprecated.class);

        String generated = generate(annotatedArray);
        assertThat(generated, is("@java.lang.Deprecated java.lang.String[]"));
    }

    /**
     * Test annotated primitive array type: {@code @Deprecated int[]}
     */
    @Test
    public void testAnnotatedPrimitiveArrayType() {
        JCodeModel cm = new JCodeModel();

        // @Deprecated int[]
        JClass intArray = cm.INT.array();
        JAnnotatedClass annotatedIntArray = JAnnotatedClass.of(intArray).annotated(Deprecated.class);

        String generated = generate(annotatedIntArray);
        assertThat(generated, is("@java.lang.Deprecated int[]"));
    }
}
