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

package org.jsonschema2pojo.integration;

import org.apache.commons.io.output.NullWriter;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jsonschema2pojo.integration.util.Compiler;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

/**
 * <p>Tests looking for warning coming from generated output.</p>
 *
 * <p>Notes: The eclipse compiler used in these tests has an open issue with the SuppressWarnings annotation.  As a result, some warnings must be
 * accepted here that would not be present in practice.
 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=469725">Bug 469725 - ECJ compiler: @SuppressWarnings annotation is ignored when ecj is invoked via java compiler tool API</a>
 * </p>
 *
 * @author Christian Trimble
 */
public class CompilerWarningIT extends Jsonschema2PojoTestBase {
    @BeforeAll
    public static void enableCapture() {
        captureDiagnostics = true;
    }

    public static Stream<Arguments> parameters() {
        JavaCompiler systemJavaCompiler = Compiler.systemJavaCompiler();
        JavaCompiler eclipseCompiler = Compiler.eclipseCompiler();
        return Stream.of(
                Arguments.of(
                        "includeAccessorsWithSystemJavaCompiler",
                        systemJavaCompiler,
                        config("includeDynamicAccessors", true, "includeDynamicGetters", true, "includeDynamicSetters", true, "includeDynamicBuilders", true),
                        "/schema/dynamic/parentType.json",
                        Matchers.empty()
                ),
                Arguments.of(
                        "includeAccessorsWithEclipseCompiler",
                        eclipseCompiler,
                        config("includeDynamicAccessors", true, "includeDynamicGetters", true, "includeDynamicSetters", true, "includeDynamicBuilders", true),
                        "/schema/dynamic/parentType.json",
                        onlyCastExceptions()
                )
        );
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("parameters")
    public void checkWarnings(String label, JavaCompiler compiler, Map<String, Object> config, String schema, Matcher<List<Diagnostic<? extends JavaFileObject>>> matcher) {
        generate(schema, "com.example", config);
        compile(compiler, new NullWriter(), new ArrayList<>(), config);

        List<Diagnostic<? extends JavaFileObject>> warnings = warnings(getDiagnostics());

        assertThat(warnings, matcher);
    }

    /**
     * Filter only warnings
     */
    public static List<Diagnostic<? extends JavaFileObject>> warnings(Collection<Diagnostic<? extends JavaFileObject>> all) {
        return all.stream().filter(entry -> entry.getKind() == Kind.WARNING).collect(Collectors.toList());
    }

    public static Matcher<Iterable<Diagnostic<? extends JavaFileObject>>> onlyCastExceptions() {
        return Matchers.everyItem(hasMessage(containsString("Type safety: Unchecked cast from")));
    }

    public static Matcher<Diagnostic<? extends JavaFileObject>> hasMessage(Matcher<String> messageMatcher) {
        return new FeatureMatcher<Diagnostic<? extends JavaFileObject>, String>(messageMatcher, "message", "message") {
            @Override
            protected String featureValueOf(Diagnostic<? extends JavaFileObject> actual) {
                return actual.getMessage(Locale.ENGLISH);
            }
        };
    }
}
