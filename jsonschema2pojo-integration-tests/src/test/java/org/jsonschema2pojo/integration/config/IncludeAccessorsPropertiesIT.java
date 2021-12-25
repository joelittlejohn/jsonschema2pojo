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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

/**
 * Checks general properties of includeAccessors and different configurations.
 *
 * @author Christian Trimble
 *
 */
@SuppressWarnings({"rawtypes"})
public class IncludeAccessorsPropertiesIT extends Jsonschema2PojoTestBase {
    public static final String PACKAGE = "com.example";
    public static final String PRIMITIVE_JSON = "/schema/properties/primitiveProperties.json";
    public static final String PRIMITIVE_TYPE = "com.example.PrimitiveProperties";

    public static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(PRIMITIVE_JSON, PRIMITIVE_TYPE, config()),
                Arguments.of(PRIMITIVE_JSON, PRIMITIVE_TYPE, config("useJodaDates", true)),
                Arguments.of(PRIMITIVE_JSON, PRIMITIVE_TYPE, config("includeAdditionalProperties", false))
        );
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void noGettersOrSettersWhenFalse(String path, String typeName, Map<String, Object> config) throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, configWithIncludeAccessors(config, false));
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("getters and setters should not exist", generatedType.getDeclaredMethods(), everyItemInArray(anyOf(methodWhitelist(), not(fieldGetterOrSetter()))));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void hasGettersOrSettersWhenTrue(String path, String typeName, Map<String, Object> config) throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, configWithIncludeAccessors(config, true));
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("a getter or setter should be found.", generatedType.getDeclaredMethods(), hasItemInArray(allOf(not(methodWhitelist()), fieldGetterOrSetter())));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void onlyHasPublicInstanceFieldsWhenFalse(String path, String typeName, Map<String, Object> config) throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, configWithIncludeAccessors(config, false));
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("only public instance fields exist", generatedType.getDeclaredFields(), everyItemInArray(anyOf(hasModifiers(Modifier.STATIC), fieldWhitelist(), hasModifiers(Modifier.PUBLIC))));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void noPublicInstanceFieldsWhenTrue(String path, String typeName, Map<String, Object> config) throws ClassNotFoundException, SecurityException {
        ClassLoader resultsClassLoader = generateAndCompile(path, PACKAGE, configWithIncludeAccessors(config, true));
        Class generatedType = resultsClassLoader.loadClass(typeName);

        assertThat("only public instance fields exist", generatedType.getDeclaredFields(), everyItemInArray(anyOf(not(hasModifiers(Modifier.PUBLIC)), fieldWhitelist())));
    }

    private static Map<String, Object> configWithIncludeAccessors(Map<String, Object> template, boolean includeAccessors) {
        Map<String, Object> config = new HashMap<>(template);
        config.put("includeGetters", includeAccessors);
        config.put("includeSetters", includeAccessors);
        return config;
    }

    private static <M extends Member> Matcher<M> hasModifiers(final int modifiers) {
        return new TypeSafeMatcher<M>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has modifier ").appendValue(Modifier.toString(modifiers));
            }

            @Override
            protected boolean matchesSafely(M item) {
                int masked = item.getModifiers() & modifiers;
                return masked == modifiers;
            }
        };
    }

    private static <M extends Member> Matcher<M> nameMatches(final Matcher<String> nameMatcher) {
        return new TypeSafeMatcher<M>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("name ").appendDescriptionOf(nameMatcher);
            }

            @Override
            protected boolean matchesSafely(M item) {
                return nameMatcher.matches(item.getName());
            }
        };
    }

    private static <T> Matcher<T[]> everyItemInArray(final Matcher<T> itemMatcher) {
        return new TypeSafeDiagnosingMatcher<T[]>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("every item in array is ").appendDescriptionOf(itemMatcher);
            }

            @Override
            protected boolean matchesSafely(T[] items, Description mismatchDescription) {
                for (T item : items) {
                    if (!itemMatcher.matches(item)) {
                        mismatchDescription.appendText("an item ");
                        itemMatcher.describeMismatch(item, mismatchDescription);
                        return false;
                    }
                }
                return true;
            }

        };
    }

    private static <M extends Member> Matcher<M> methodWhitelist() {
        return nameMatches(isIn(Arrays.asList("setAdditionalProperty", "getAdditionalProperties")));
    }

    private static <M extends Member> Matcher<M> fieldWhitelist() {
        return nameMatches(isIn(Collections.singletonList("additionalProperties")));
    }

    private static <M extends Member> Matcher<M> fieldGetterOrSetter() {
        return nameMatches(anyOf(startsWith("get"), startsWith("set")));
    }
}
