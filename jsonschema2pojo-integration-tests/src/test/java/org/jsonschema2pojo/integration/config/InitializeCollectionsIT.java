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

package org.jsonschema2pojo.integration.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass(name="{0}")
@MethodSource("parameters")
public class InitializeCollectionsIT {
    
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

    @RegisterExtension public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private Map<String, Object> config;
    private Matcher<Object> resultMatcher;
    private String getterName;
    
    public InitializeCollectionsIT(String label, Map<String, Object> config, String getterName, Matcher<Object> resultMatcher) {
        this.config = config;
        this.getterName = getterName;
        this.resultMatcher = resultMatcher;
    }

    @Test
    public void correctResult() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/initializeCollectionProperties.json", "com.example", config);

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.InitializeCollectionProperties");
        Object instance = generatedType.newInstance();

        Method getter = generatedType.getMethod(getterName);

        assertThat(getter.invoke(instance), resultMatcher);
    }

}
