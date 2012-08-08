/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.integration.util;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.StringUtils;

/**
 * Compiles all the Java source files found in a given directory using the
 * JSR-199 API in Java 6.
 */
public class Compiler {

    private static final String PRINT_SOURCE_PROPERTY = "printSource";

    public void compile(File directory) {

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(findAllSourceFiles(directory));

        if (compilationUnits.iterator().hasNext()) {
            Boolean success = javaCompiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
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
        if (StringUtils.equals(System.getProperty(PRINT_SOURCE_PROPERTY), "true")) {
            try {
                System.out.println(readFileToString(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
