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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


/**
 * Compiles all the Java source files found in a given directory using the
 * JSR-199 API in Java 6.
 */
public class Compiler {

    public void compile(File sourceDirectory, File outputDirectory, List<File> classpath, String targetVersion ) {
      compile(null, null, sourceDirectory, outputDirectory, classpath, null, targetVersion);
    }

    public void compile(JavaCompiler javaCompiler, Writer out, File sourceDirectory, File outputDirectory, List<File> classpath, DiagnosticListener<? super JavaFileObject> diagnosticListener, String targetVersion ) {
        targetVersion = targetVersion == null ? "1.6" : targetVersion;

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
        Collection<File> files = listFiles(directory, new String[] { "java" }, true);

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
                System.out.println(readFileToString(file));
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
}
