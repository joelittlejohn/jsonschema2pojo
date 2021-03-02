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
    public void testNonLatinNames() {
        assertThat(nameHelper.getBuilderName("foo", NODE), is("withFoo"));
        assertThat(nameHelper.getBuilderName("ƈoƅ", NODE), is("withO"));
        assertThat(nameHelper.getBuilderName("URL", NODE), is("withUrl"));
        assertThat(nameHelper.getBuilderName("УРЛ", NODE), is("with"));
        assertThat(nameHelper.getBuilderName("  *   УРЛ", NODE), is("with"));
        assertThat(nameHelper.getBuilderName("0УРЛ", NODE), is("with0"));

        assertThat(nameHelper.getGetterName("foo", new JCodeModel().BOOLEAN, NODE), is("isFoo"));
        assertThat(nameHelper.getGetterName("ƈoƅ", new JCodeModel().BOOLEAN, NODE), is("isO"));
        assertThat(nameHelper.getGetterName("URL", new JCodeModel().INT, NODE), is("getUrl"));
        assertThat(nameHelper.getGetterName("  *   УРЛ", new JCodeModel().INT, NODE), is("get"));
        assertThat(nameHelper.getGetterName("0УРЛ", new JCodeModel().INT, NODE), is("get0"));

        assertThat(nameHelper.getSetterName("foo", NODE), is("setFoo"));
        assertThat(nameHelper.getSetterName("ƈoƅ", NODE), is("setO"));
        assertThat(nameHelper.getSetterName("URL", NODE), is("setUrl"));
        assertThat(nameHelper.getSetterName("  *   УРЛ", NODE), is("set"));
        assertThat(nameHelper.getSetterName("0УРЛ", NODE), is("set0"));

        // ALLOW NON-LATIN
        NameHelper nameHelper = helper(false, true);

        assertThat(nameHelper.getBuilderName("foo", NODE), is("withFoo"));
        assertThat(nameHelper.getBuilderName("ƈoƅ", NODE), is("withƇoƅ"));
        assertThat(nameHelper.getBuilderName("URL", NODE), is("withUrl"));
        assertThat(nameHelper.getBuilderName("  *   УРЛ", NODE), is("with______урл"));
        assertThat(nameHelper.getBuilderName("0УРЛ", NODE), is("with_0урл"));

        assertThat(nameHelper.getGetterName("foo", new JCodeModel().BOOLEAN, NODE), is("isFoo"));
        assertThat(nameHelper.getGetterName("ƈoƅ", new JCodeModel().BOOLEAN, NODE), is("isƇoƅ"));
        assertThat(nameHelper.getGetterName("URL", new JCodeModel().INT, NODE), is("getUrl"));
        assertThat(nameHelper.getGetterName("  *   УРЛ", new JCodeModel().INT, NODE), is("get______урл"));
        assertThat(nameHelper.getGetterName("0УРЛ", new JCodeModel().INT, NODE), is("get_0урл"));

        assertThat(nameHelper.getSetterName("foo", NODE), is("setFoo"));
        assertThat(nameHelper.getSetterName("ƈoƅ", NODE), is("setƇoƅ"));
        assertThat(nameHelper.getSetterName("URL", NODE), is("setUrl"));
        assertThat(nameHelper.getSetterName("  *   УРЛ", NODE), is("set______урл"));
        assertThat(nameHelper.getSetterName("0УРЛ", NODE), is("set_0урл"));
    }

    private NameHelper helper(boolean useTitleAsClassname) {
        return helper(useTitleAsClassname, false);
    }

    private NameHelper helper(boolean useTitleAsClassname, boolean allowNonLatinNames) {
        GenerationConfig config = mock(GenerationConfig.class);
        when(config.isUseTitleAsClassname()).thenReturn(useTitleAsClassname);
        when(config.isAllowNonLatinNames()).thenReturn(allowNonLatinNames);
        return new NameHelper(config);
    }

    private ObjectNode node(String key, String value) {
        return JsonNodeFactory.instance.objectNode()
                .put(key, value);
    }
}
