/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

@RunWith(Parameterized.class)
public class InitializeCollectionsIT {
    
    @Parameters(name="{0}")
    public static Collection<Object[]> parameters() {
        Map<String, Object> withOptionFalse = config("initializeCollections", false);
        Map<String, Object> withOptionAbsent = config();
        return Arrays.asList(new Object[][] {
            {"defaultValueForCollectionsIsEmptyCollection", withOptionAbsent, "getList", notNullValue()},
            {"defaultValueForListIsNullWithProperty", withOptionFalse, "getList", nullValue()},
            {"defaultValueForSetIsNullWithProperty", withOptionFalse, "getSet", nullValue()},
            {"defaultValueForListWithValuesIsNotNullWithProperty", withOptionFalse, "getListWithValues", notNullValue()},
            {"defaultValueForSetWithValuesIsNotNullWithProperty", withOptionFalse, "getSetWithValues", notNullValue()}
        });
    }

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private Map<String, Object> config;
    private Matcher<Object> resultMatcher;
    private String getterName;
    
    public InitializeCollectionsIT(String label, Map<String, Object> config, String getterName, Matcher<Object> resultMatcher) {
        this.config = config;
        this.getterName = getterName;
        this.resultMatcher = resultMatcher;
    }

    @Test
    public void correctResult() throws ClassNotFoundException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/initializeCollectionProperties.json", "com.example", config);

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.InitializeCollectionProperties");
        Object instance = generatedType.newInstance();

        Method getter = generatedType.getMethod(getterName);

        assertThat(getter.invoke(instance), resultMatcher);
    }

}
