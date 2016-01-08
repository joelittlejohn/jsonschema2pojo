/**
 * Copyright Â© 2010-2014 Nokia
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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.integration.util.CodeGenerationHelper;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

import com.sun.codemodel.JCodeModel;

public class SelfRef2IT {

    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void multipleNestedSelfReferencesToSameTypeDefinitions() throws NoSuchMethodException, ClassNotFoundException, IOException {

        URL schemaUrl = CodeGenerationHelper.class.getResource("/schema/ref/selfRefs2.jsonschema");
        JCodeModel codeModel = new JCodeModel();
        
        // NOTE: using generate with last argument being the loaded json schema contents (String) will make this test fail!!!!
        new SchemaMapper().generate(codeModel, "SelfRefs2", "com.example", schemaUrl);

        codeModel.build(schemaRule.getGenerateDir());

        ClassLoader classLoader = schemaRule.compile();

        Class<?> topClass = classLoader.loadClass("com.example.SelfRefs2");
        assertThat(topClass.getMethod("getFirstRef").getReturnType().getSimpleName(), equalTo("AType"));
        assertThat(topClass.getMethod("getSecondRef").getReturnType().getSimpleName(), equalTo("AType"));

        Class<?> firstLevelClass = classLoader.loadClass("com.example.AType");
        assertThat(firstLevelClass.getMethod("getFirstProp").getReturnType().getSimpleName(), equalTo("BType"));
        assertThat(firstLevelClass.getMethod("getSecondProp").getReturnType().getSimpleName(), equalTo("BType"));

        Class<?> secondLevelClass = classLoader.loadClass("com.example.BType");
        assertThat(secondLevelClass.getMethod("getFirstProp").getReturnType().getSimpleName(), equalTo("String"));
        assertThat(secondLevelClass.getMethod("getSecondProp").getReturnType().getSimpleName(), equalTo("String"));
    }
}
