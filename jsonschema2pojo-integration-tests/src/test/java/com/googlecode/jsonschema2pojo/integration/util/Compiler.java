/**
 * Copyright © 2011 Nokia
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
import static org.apache.commons.lang.StringUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.compilers.JavaCompilerSettings;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Compiles java source files in a given output directory using Apache Commons
 * JCI.
 */
public class Compiler {
    
    private static final String PRINT_SOURCE_PROPERTY = "printSource";
    
    public void compile(File outputDirectory) {
        
        JavaCompiler compiler = new JavaCompilerFactory().createCompiler("eclipse");
        
        JavaCompilerSettings compilerSettings = compiler.createDefaultSettings();
        compilerSettings.setSourceVersion("1.5");
        compilerSettings.setTargetVersion("1.5");
        
        CompilationResult result = compiler.compile(findAllSourceFiles(outputDirectory), new FileResourceReader(outputDirectory), new FileResourceStore(outputDirectory), Thread.currentThread().getContextClassLoader(), compilerSettings);
        
        assertThat(ArrayUtils.toString(result.getErrors()), result.getErrors().length, is(0));
    }
    
    @SuppressWarnings("unchecked")
    private String[] findAllSourceFiles(File outputDirectory) {
        
        List<String> javaSourceFileNames = new ArrayList<String>();
        
        for (File file : (Collection<File>) listFiles(outputDirectory, new String[] { "java" }, true)) {
            debugOutput(file);
            
            javaSourceFileNames.add(removeStart(file.getAbsolutePath(), outputDirectory.getAbsolutePath() + File.separator));
        }
        
        return javaSourceFileNames.toArray(new String[javaSourceFileNames.size()]);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_ALWAYS_NULL", 
            justification="Findbugs bug: false positive when using System.out, http://old.nabble.com/-FB-Discuss--Problems-with-false(-)positive-on-System.out.println-td30586499.html")
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
