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

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * Compiles all the Java source files found in a given directory using the
 * JSR-199 API in Java 6.
 */
public class Compiler {

    public void compile(File sourceDirectory, File outputDirectory, List<File> classpath, String targetVersion) {
        // TODO: invalid test, as JavaCompiler can't be null in the next method
        compile(null, null, sourceDirectory, outputDirectory, classpath, null, targetVersion);
    }

    public void compile(JavaCompiler javaCompiler, Writer out, File sourceDirectory, File outputDirectory, List<File> classpath, DiagnosticListener<? super JavaFileObject> diagnosticListener, String targetVersion) {
        Objects.requireNonNull(javaCompiler, "Java compiler must not be null");
        targetVersion = targetJavaVersion(targetVersion);

        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);

        if (outputDirectory != null) {
            try {
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                        Collections.singletonList(outputDirectory));
                fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
            } catch (IOException e) {
                throw new RuntimeException("could not set output directory", e);
            }
        }

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(findAllSourceFiles(sourceDirectory));

        ArrayList<String> options = new ArrayList<>();
        options.add("-source");
        options.add(targetVersion);
        options.add("-target");
        options.add(targetVersion);
        options.add("-encoding");
        options.add("UTF8");
        options.add("-Xlint:-options");
        options.add("-Xlint:unchecked");
        if (compilationUnits.iterator().hasNext()) {
            Boolean success = javaCompiler.getTask(out, fileManager, diagnosticListener, options, null, compilationUnits).call();
            assertThat("Compilation was not successful, check stdout for errors", success, is(true));
        }

    }

    private Collection<File> findAllSourceFiles(File directory) {
        Collection<File> files = listFiles(directory, new String[]{"java"}, true);

        for (File file : files) {
            debugOutput(file);
        }

        return files;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL",
            justification = "Findbugs bug: false positive when using System.out, http://old.nabble.com/-FB-Discuss--Problems-with-false(-)positive-on-System.out.println-td30586499.html")
    private void debugOutput(File file) {

        if (System.getProperty("debug") != null) {
            try {
                System.out.println(readFileToString(file, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static JavaCompiler systemJavaCompiler() {
        return ToolProvider.getSystemJavaCompiler();
    }

    public static JavaCompiler eclipseCompiler() {
        return new EclipseCompiler();
    }

    public static String targetJavaVersion(String targetVersion) {
        final int runtimeJavaVersion = getJavaVersion();
        final int minimalTargetVersion;
        if (runtimeJavaVersion > 9) {
            minimalTargetVersion = 7;
        } else {
            minimalTargetVersion = 6;
        }
        if (targetVersion == null) {
            return "1." + minimalTargetVersion;
        }
        final int targetIntVersion = getJavaVersion(targetVersion);
        if (targetIntVersion > minimalTargetVersion) {
            return targetVersion;
        }
        return "1." + minimalTargetVersion;
    }

    public static int getJavaVersion() {
        return getJavaVersion(System.getProperty("java.version"));
    }

    private static int getJavaVersion(String version) {
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }
}
