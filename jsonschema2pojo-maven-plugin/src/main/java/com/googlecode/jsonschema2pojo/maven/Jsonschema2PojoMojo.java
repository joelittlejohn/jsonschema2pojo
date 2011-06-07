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

package com.googlecode.jsonschema2pojo.maven;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.googlecode.jsonschema2pojo.cli.Jsonschema2Pojo;
import com.googlecode.jsonschema2pojo.rules.RuleFactory;

/**
 * When invoked, this goal reads one or more <a
 * href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @see <a
 *      href="http://maven.apache.org/developers/mojo-api-specification.html">Mojo
 *      API Specification</a>
 */
public class Jsonschema2PojoMojo extends AbstractMojo {

    /**
     * Target directory for generated Java source files.
     * 
     * @parameter expression="${jsonschema2pojo.outputDirectory}"
     *            default-value="${project.build.directory}/java-gen"
     * @since 0.1.0
     */
    private File outputDirectory;

    /**
     * Location of the JSON Schema file(s). Despite the fact that this is
     * parameter uses 'directory' in its name, it may refer to a single file or
     * a directory of files.
     * 
     * @parameter expression="${jsonschema2pojo.sourceDirectory}"
     * @required
     * @since 0.1.0
     */
    private File sourceDirectory;

    /**
     * Package name used for generated Java classes.
     * 
     * @parameter expression="${jsonschema2pojo.targetPackage}"
     * @required
     * @since 0.1.0
     */
    private String targetPackage;

    /**
     * Whether or not to generate builder-style setters alongside the
     * void-return ones.
     * 
     * @parameter expression="${jsonschema2pojo.generateBuilders}"
     *            default-value="false"
     * @since 0.1.2
     */
    private boolean generateBuilders;

    /**
     * Add the output directory to the project as a source root, so that the
     * generated java types are compiled and included in the project artifact.
     * 
     * @parameter expression="${jsonschema2pojo.addCompileSourceRoot}"
     *            default-value="true"
     * @since 0.1.9
     */
    private boolean addCompileSourceRoot = true;

    /**
     * The project being built.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Executes the plugin, to read the given source and behavioural properties
     * and generate POJOs. The current implementation acts as a wrapper around
     * the command line interface.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "Private fields set by Maven.")
    public void execute() throws MojoExecutionException {

        if (addCompileSourceRoot) {
            project.addCompileSourceRoot(outputDirectory.getPath());
        }

        addProjectDependenciesToClasspath();

        Map<String, String> behaviourProperties = new HashMap<String, String>();
        behaviourProperties.put(RuleFactory.GENERATE_BUILDERS_PROPERTY, "" + generateBuilders);

        try {
            Jsonschema2Pojo.generate(sourceDirectory, targetPackage, outputDirectory, behaviourProperties);
        } catch (IOException e) {
            throw new MojoExecutionException("Error generating classes from JSON Schema file(s) " + sourceDirectory.getPath(), e);
        }

    }

    private void addProjectDependenciesToClasspath() {

        try {

            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
            Thread.currentThread().setContextClassLoader(newClassLoader);

        } catch (DependencyResolutionRequiredException e) {
            getLog().info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", e);
        }

    }

}
