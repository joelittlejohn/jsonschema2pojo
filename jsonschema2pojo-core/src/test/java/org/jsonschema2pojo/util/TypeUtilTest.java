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

import java.util.List;

import org.junit.Test;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;

public class TypeUtilTest {

    @Test
    public void testResolveTypeCanHandleWildcard() {
        final JCodeModel codeModel = new JCodeModel();
        final JClass _class = TypeUtil.resolveType(codeModel.rootPackage(), "java.util.List<?>");

        assertThat(_class.erasure(), equalTo(codeModel.ref(List.class)));
        assertThat(_class.typeParams(), emptyArray());
        assertThat(_class.isParameterized(), is(Boolean.TRUE));
        assertThat(_class.getTypeParameters(), hasSize(1));
        assertThat(_class.getTypeParameters().get(0)._extends(), is(equalTo(codeModel.ref(Object.class))));
    }

    @Test
    public void testResolveTypeCanHandleExtendsWildcard() {
        final JCodeModel codeModel = new JCodeModel();
        final JClass _class = TypeUtil.resolveType(codeModel.rootPackage(), "java.util.List<? extends java.lang.Number>");

        assertThat(_class.erasure(), equalTo(codeModel.ref(List.class)));
        assertThat(_class.typeParams(), emptyArray());
        assertThat(_class.isParameterized(), is(Boolean.TRUE));
        assertThat(_class.getTypeParameters(), hasSize(1));
        assertThat(_class.getTypeParameters().get(0)._extends(), is(equalTo(codeModel.ref(Number.class))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveTypeForSuperWildcardThrowsException() {
        TypeUtil.resolveType(new JCodeModel().rootPackage(), "java.util.List<? super java.lang.String>");
    }
}
