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

package org.jsonschema2pojo.integration.util;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.maven.Jsonschema2PojoMojo;

public class CodeGenerationHelper {

    public static File generate(String schema, String targetPackage) {
        Map<String, Object> configValues = Collections.emptyMap();
        return generate(schema, targetPackage, configValues);
    }

    public static File generate(URL schema, String targetPackage) {
        Map<String, Object> configValues = Collections.emptyMap();
        return generate(schema, targetPackage, configValues);
    }

    public static File generate(String schema, String targetPackage, Map<String, Object> configValues) {
        URL schemaUrl = CodeGenerationHelper.class.getResource(schema);
        assertThat("Unable to read schema resource from the classpath: " + schema, schemaUrl, is(notNullValue()));

        return generate(schemaUrl, targetPackage, configValues);
    }

    /**
     * Invokes the jsonschema2pojo plugin to generate Java types from a given
     * schema.
     * 
     * @param schema
     *            a classpath resource to be used as the input JSON Schema
     * @param targetPackage
     *            the default target package for generated classes
     * @param configValues
     *            the generation config options and values that should be set on
     *            the maven plugin before invoking it
     */
    public static File generate(final URL schema, final String targetPackage, final Map<String, Object> configValues) {
        final File outputDirectory = createTemporaryOutputFolder();

        generate(schema, targetPackage, configValues, outputDirectory);

        return outputDirectory;
    }

    public static void generate(final URL schema, final String targetPackage, final Map<String, Object> configValues, final File outputDirectory) {
        try {
            generate(new File(schema.toURI()), targetPackage, configValues, outputDirectory);
          } catch (URISyntaxException e) {
              throw new RuntimeException(e);
          }
      }
      
      public static File generate(final File sourceDirectory, final String targetPackage) {
          Map<String, Object> configValues = Collections.emptyMap();

          return generate(sourceDirectory, targetPackage, configValues);
      }
      
      public static File generate(final File sourceDirectory, final String targetPackage, final Map<String, Object> configValues) {
          final File outputDirectory = createTemporaryOutputFolder();

          generate(sourceDirectory, targetPackage, configValues, outputDirectory);

          return outputDirectory;
      }
      
      public static void generate(final File sourceDirectory, final String targetPackage, final Map<String, Object> configValues, final File outputDirectory) {

        try {
            @SuppressWarnings("serial")
        Jsonschema2PojoMojo pluginMojo = new TestableJsonschema2PojoMojo().configure(new HashMap<String, Object>() {
                {
                    put("sourceDirectory", sourceDirectory);
                    put("outputDirectory", outputDirectory);
                    put("project", getMockProject());
                    put("targetPackage", targetPackage);
                    putAll(configValues);
                }
            });

            pluginMojo.execute();
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        }

    }

    private static MavenProject getMockProject() throws DependencyResolutionRequiredException {

        MavenProject project = mock(MavenProject.class);
        when(project.getCompileClasspathElements()).thenReturn(new ArrayList<String>());

        return project;
    }

    /**
     * Compiles the source files in a given directory.
     * 
     * @param sourceDirectory
     *            the directory containing Java source to be compiled.
     * @return a classloader which will provide access to any classes that were
     *         generated by the plugin.
     */
    public static ClassLoader compile(File sourceDirectory) {

        return compile(sourceDirectory, new ArrayList<String>());

    }

    public static ClassLoader compile(File sourceDirectory, List<String> classpath) {

        List<String> fullClasspath = new ArrayList<String>();
        fullClasspath.addAll(classpath);
        fullClasspath.add(System.getProperty("java.class.path"));

        new Compiler().compile(sourceDirectory, join(fullClasspath, File.pathSeparatorChar));

        try {
            return URLClassLoader.newInstance(new URL[] { sourceDirectory.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Invokes the jsonschema2pojo plugin then compiles the resulting source.
     * 
     * @param schema
     *            a classpath resource to be used as the input JSON Schema.
     * @param targetPackage
     *            the default target package for generated classes.
     * @param configValues
     *            the generation config options and values that should be set on
     *            the maven plugin before invoking it
     * @return a classloader which will provide access to any classes that were
     *         generated by the plugin.
     */
    public static ClassLoader generateAndCompile(String schema, String targetPackage, Map<String, Object> configValues) {

        File outputDirectory = generate(schema, targetPackage, configValues);

        return compile(outputDirectory);

    }

    public static ClassLoader generateAndCompile(String schema, String targetPackage) {

        File outputDirectory = generate(schema, targetPackage);

        return compile(outputDirectory);

    }

    public static ClassLoader generateAndCompile(File sourceDirectory, String targetPackage) {

        File outputDirectory = generate(sourceDirectory, targetPackage);

        return compile(outputDirectory);

    }
    
    public static ClassLoader generateAndCompile(File sourceDirectory, String targetPackage, Map<String, Object> configValues) {

        File outputDirectory = generate(sourceDirectory, targetPackage, configValues);

        return compile(outputDirectory);

    }

    public static File createTemporaryOutputFolder() {

        String tempDirectoryName = System.getProperty("java.io.tmpdir");
        String outputDirectoryName = tempDirectoryName + File.separator + UUID.randomUUID().toString();

        final File outputDirectory = new File(outputDirectoryName);

        try {
            outputDirectory.mkdir();
        } finally {
            deleteOnExit(outputDirectory);
        }

        return outputDirectory;
    }

    /**
     * Deletes temporary output files on exit <em>recursively</em> (which is not
     * possible with {@link File#deleteOnExit}).
     * 
     * @param outputDirectory
     *            the directory to be deleted.
     */
    private static void deleteOnExit(final File outputDirectory) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteDirectory(outputDirectory);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    public static Map<String, Object> config(Object... keyValuePairs) {

        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid config, uneven list of key/value pairs!");
        }

        Map<String, Object> values = new HashMap<String, Object>();
        for (int i = 0; i < keyValuePairs.length; i = i + 2) {
            values.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
        }

        return values;
    }
}
