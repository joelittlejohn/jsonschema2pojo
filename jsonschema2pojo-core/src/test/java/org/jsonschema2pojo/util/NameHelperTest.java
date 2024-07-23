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

package org.jsonschema2pojo.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JCodeModel;

public class NameHelperTest {

    private static final ObjectNode NODE = JsonNodeFactory.instance.objectNode();

    private final NameHelper nameHelper = new NameHelper(new DefaultGenerationConfig());

    @Test
    public void testGetterNamedCorrectly() {
        assertThat(nameHelper.getGetterName("foo", new JCodeModel().BOOLEAN, NODE), is("isFoo"));
        assertThat(nameHelper.getGetterName("foo", new JCodeModel().INT, NODE), is("getFoo"));
        assertThat(nameHelper.getGetterName("oAuth2State", new JCodeModel().INT, NODE), is("getoAuth2State"));
        assertThat(nameHelper.getGetterName("URL", new JCodeModel().INT, NODE), is("getUrl"));
    }

    @Test
    public void testSetterNamedCorrectly() {
        assertThat(nameHelper.getSetterName("foo", NODE), is("setFoo"));
        assertThat(nameHelper.getSetterName("oAuth2State", NODE), is("setoAuth2State"));
        assertThat(nameHelper.getSetterName("URL", NODE), is("setUrl"));
    }

    @Test
    public void testBuilderNamedCorrectly() {
        assertThat(nameHelper.getBuilderName("foo", NODE), is("withFoo"));
        assertThat(nameHelper.getBuilderName("oAuth2State", NODE), is("withoAuth2State"));
        assertThat(nameHelper.getBuilderName("URL", NODE), is("withUrl"));
    }

    @Test
    public void testClassNameCorrectly() {
        assertThat(nameHelper.getClassName("foo", NODE), is("foo"));
        assertThat(nameHelper.getClassName("foo", node("title", "bar")), is("foo"));
        assertThat(nameHelper.getClassName("foo", node("javaName", "bar")), is("bar"));
        assertThat(nameHelper.getClassName("foo", node("javaName", "bar").put("title", "abc")), is("bar"));

        // TITLE_ATTRIBUTE
        NameHelper nameHelper = helper(true);
        assertThat(nameHelper.getClassName("foo", node("title", "bar")), is("Bar"));
        assertThat(nameHelper.getClassName("foo", node("title", "i am bar")), is("IAmBar"));
        assertThat(nameHelper.getClassName("foo", node("javaName", "bar")), is("bar"));
        assertThat(nameHelper.getClassName("foo", node("javaName", "bar").put("title", "abc")), is("bar"));
    }

    @Test
    public void testGetUniqueClassNamePackageNewClass() throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JPackage _package = codeModel._package("pkg");

        assertThat(nameHelper.getUniqueClassName("foo", NODE, _package), is("Foo"));

        // Repeat to make sure the name stays the same on subsequent invocation
        assertThat(nameHelper.getUniqueClassName("foo", NODE, _package), is("Foo"));
    }

    @Test
    public void testGetUniqueClassNamePackageExistingClass() throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JPackage _package = codeModel._package("pkg");
        _package._class("Foo");

        assertThat(nameHelper.getUniqueClassName("foo", NODE, _package), is("Foo__1"));

        // Repeat to make sure the name stays the same on subsequent invocation
        assertThat(nameHelper.getUniqueClassName("foo", NODE, _package), is("Foo__1"));
    }

    @Test
    public void testGetUniqueClassNameDefinedClassNewClass() throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass _class = codeModel._class("Foo");

        assertThat(nameHelper.getUniqueClassName("bar", NODE, _class), is("Bar"));

        // Repeat to make sure the name stays the same on subsequent invocation
        assertThat(nameHelper.getUniqueClassName("bar", NODE, _class), is("Bar"));
    }

    @Test
    public void testGetUniqueClassNameDefinedClassExistingClass() throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass _class = codeModel._class("Foo");
        _class._class("Bar");

        assertThat(nameHelper.getUniqueClassName("bar", NODE, _class), is("Bar__1"));

        // Repeat to make sure the name stays the same on subsequent invocation
        assertThat(nameHelper.getUniqueClassName("bar", NODE, _class), is("Bar__1"));
    }

    private NameHelper helper(boolean useTitleAsClassname) {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.isUseTitleAsClassname()).thenReturn(useTitleAsClassname);
        return new NameHelper(config);
    }

    private ObjectNode node(String key, String value) {
        return JsonNodeFactory.instance.objectNode()
                .put(key, value);
    }
}
