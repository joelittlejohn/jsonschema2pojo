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

package org.jsonschema2pojo.integration.util;

import org.jsonschema2pojo.test.JUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jsonschema2pojo.integration.util.Compiler.systemJavaCompiler;

public class Jsonschema2PojoTestBase extends JUnitTestBase {
    @TempDir
    public File generateDir;
    @TempDir
    public File compileDir;

    protected static boolean captureDiagnostics = false;

    private final Collection<Diagnostic<? extends JavaFileObject>> diagnostics = new ConcurrentLinkedQueue<>();

    public File getGenerateDir() {
        return generateDir;
    }

    public File getCompileDir() {
        return compileDir;
    }

    public Collection<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }

    @BeforeEach
    public void captureBeforeEach() {
        diagnostics.clear();
    }

    public File generate(String schema, String targetPackage) {
        return generate(schema, targetPackage, emptyConfig());
    }

    public File generate(URL schema, String targetPackage) {
        return generate(schema, targetPackage, emptyConfig());
    }

    public File generate(String schema, String targetPackage, Map<String, Object> configValues) {
        return generate(schemaUrl(schema), targetPackage, configValues);
    }

    public File generate(final URL schema, final String targetPackage, final Map<String, Object> configValues) {
        CodeGenerationHelper.generate(schema, targetPackage, configValues, getGenerateDir());
        return generateDir;
    }

    public ClassLoader compile() {
        return compile(emptyClasspath(), emptyConfig());
    }

    public ClassLoader compile(List<File> classpath) {
        return compile(classpath, emptyConfig());
    }

    public ClassLoader compile(List<File> classpath, Map<String, Object> config) {
        return compile(systemJavaCompiler(), null, classpath, config);
    }

    public ClassLoader compile(JavaCompiler compiler, Writer out, List<File> classpath, Map<String, Object> config) {
        DiagnosticListener<JavaFileObject> diagnosticListener = captureDiagnostics ? new CapturingDiagnosticListener() : null;
        return CodeGenerationHelper.compile(compiler, out, getGenerateDir(), getCompileDir(), classpath, config, diagnosticListener);
    }

    public ClassLoader generateAndCompile(String schema, String targetPackage, Map<String, Object> configValues) {
        generate(schema, targetPackage, configValues);
        return compile(emptyClasspath(), configValues);
    }

    public ClassLoader generateAndCompile(String schema, String targetPackage) {
        generate(schema, targetPackage);
        return compile();
    }

    public ClassLoader generateAndCompile(URL schema, String targetPackage) {
        generate(schema, targetPackage);
        return compile(emptyClasspath(), emptyConfig());
    }

    public ClassLoader generateAndCompile(URL schema, String targetPackage, Map<String, Object> configValues) {
        generate(schema, targetPackage, configValues);
        return compile(emptyClasspath(), configValues);
    }

    public File generated(String relativeSourcePath) {
        return new File(generateDir, relativeSourcePath);
    }

    private static List<File> emptyClasspath() {
        return new ArrayList<>();
    }

    private static Map<String, Object> emptyConfig() {
        return new HashMap<>();
    }

    private static URL schemaUrl(String schema) {
        URL schemaUrl = Jsonschema2PojoTestBase.class.getResource(schema);
        assertThat("Unable to read schema resource from the classpath: " + schema, schemaUrl, is(notNullValue()));
        return schemaUrl;
    }

    class CapturingDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            diagnostics.add(diagnostic);
        }
    }
}
