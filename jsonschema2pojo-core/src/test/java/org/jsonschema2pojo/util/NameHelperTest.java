/**
 * Copyright © 2010-2017 Nokia
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

import org.jsonschema2pojo.DefaultGenerationConfig;
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
        assertThat(nameHelper.getGetterName("URL", new JCodeModel().INT, NODE), is("getURL"));
    }

    @Test
    public void testSetterNamedCorrectly() {
        assertThat(nameHelper.getSetterName("foo", NODE), is("setFoo"));
        assertThat(nameHelper.getSetterName("oAuth2State", NODE), is("setoAuth2State"));
        assertThat(nameHelper.getSetterName("URL", NODE), is("setURL"));
    }

}
