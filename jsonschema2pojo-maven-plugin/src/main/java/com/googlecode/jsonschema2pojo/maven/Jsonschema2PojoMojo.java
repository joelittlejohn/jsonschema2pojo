/**
 * Copyright © 2010 Nokia
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.cli.Jsonschema2Pojo;

/**
 * @goal generate
 * @phase generate-sources
 */
public class Jsonschema2PojoMojo extends AbstractMojo {

    /**
     * Target directory for generated Java source files.
     * 
     * @parameter expression="${project.build.directory}/java-gen"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the JSON Schema file(s).
     * 
     * @parameter
     * @required
     */
    private File sourceDirectory;

    /**
     * Package name used for generated Java classes.
     * 
     * @parameter
     */
    private String targetPackage = "";

    /**
     * The project being built
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Whether or not to generate builder-style setters alongside the
     * void-return ones. Defaults to false.
     * 
     * @parameter
     */
    private String generateBuilders = "false";

    @Override
    public void execute() throws MojoExecutionException {

        project.addCompileSourceRoot(outputDirectory.getPath());

        Map<String, String> behaviourProperties = new HashMap<String, String>();
        behaviourProperties.put(SchemaMapper.GENERATE_BUILDERS_PROPERTY, generateBuilders);

        try {
            Jsonschema2Pojo.generate(sourceDirectory, targetPackage, outputDirectory, behaviourProperties);
        } catch (IOException e) {
            throw new MojoExecutionException("Error generating classes from JSON Schema file(s) " + sourceDirectory.getPath(), e);
        }

    }

}
