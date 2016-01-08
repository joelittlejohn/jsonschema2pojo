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

public class ExtRefIT {
    @Rule
    public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    
    @Test
    public void multipleExternalReferencesToDifferentTypes()  throws NoSuchMethodException, ClassNotFoundException, IOException {
        URL schemaUrl = CodeGenerationHelper.class.getResource("/schema/ref/refsToExt.jsonschema");
        JCodeModel codeModel = new JCodeModel();
        
        // NOTE: using generate with last argument being the loaded json schema contents (String) will make this test fail!!!!
        new SchemaMapper().generate(codeModel, "RefsToExt", "com.example", schemaUrl);

        codeModel.build(schemaRule.getGenerateDir());

        ClassLoader classLoader = schemaRule.compile();

        Class<?> topClass = classLoader.loadClass("com.example.RefsToExt");
        assertThat(topClass.getMethod("getFirstExtRef").getReturnType().getSimpleName(), equalTo("FirstRef"));
        assertThat(topClass.getMethod("getSecondExtRef").getReturnType().getSimpleName(), equalTo("SecondRef"));
        assertThat(topClass.getMethod("getThirdExtRef").getReturnType().getSimpleName(), equalTo("FirstRef"));
        assertThat(topClass.getMethod("getFourthExtRef").getReturnType().getSimpleName(), equalTo("SecondRef"));

        Class<?> firstExtClass = classLoader.loadClass("com.example.FirstRef");
        assertThat(firstExtClass.getMethod("getVal").getReturnType().getSimpleName(), equalTo("String"));

        Class<?> secondExtClass = classLoader.loadClass("com.example.SecondRef");
        assertThat(secondExtClass.getMethod("getVal").getReturnType().getSimpleName(), equalTo("String"));
    }
}
