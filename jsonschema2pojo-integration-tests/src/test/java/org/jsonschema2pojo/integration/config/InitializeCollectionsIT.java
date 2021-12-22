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
import java.util.Map;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class InitializeCollectionsIT extends Jsonschema2PojoTestBase {

    public static Stream<Arguments> parameters() {
        Map<String, Object> withOptionFalse = config("initializeCollections", false);
        Map<String, Object> withOptionAbsent = config();
        return Stream.of(
            Arguments.of("defaultValueForCollectionsIsEmptyCollection", withOptionAbsent, "getList", notNullValue()),
            Arguments.of("defaultValueForListIsNullWithProperty", withOptionFalse, "getList", nullValue()),
            Arguments.of("defaultValueForSetIsNullWithProperty", withOptionFalse, "getSet", nullValue()),
            Arguments.of("defaultValueForListWithValuesIsNotNullWithProperty", withOptionFalse, "getListWithValues", notNullValue()),
            Arguments.of("defaultValueForSetWithValuesIsNotNullWithProperty", withOptionFalse, "getSetWithValues", notNullValue())
        );
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void correctResult(String label, Map<String, Object> config, String getterName, Matcher<Object> resultMatcher) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/initializeCollectionProperties.json", "com.example", config);

        Class<?> generatedType = resultsClassLoader.loadClass("com.example.InitializeCollectionProperties");
        Object instance = generatedType.newInstance();

        Method getter = generatedType.getMethod(getterName);

        assertThat(getter.invoke(instance), resultMatcher);
    }

}
