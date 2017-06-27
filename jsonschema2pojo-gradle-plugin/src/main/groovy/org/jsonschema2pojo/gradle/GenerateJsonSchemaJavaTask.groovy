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
package org.jsonschema2pojo.gradle

import org.jsonschema2pojo.Jsonschema2Pojo
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * A task that performs code generation.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
class GenerateJsonSchemaJavaTask extends DefaultTask {
  def configuration

  GenerateJsonSchemaJavaTask() {
    description = 'Generates Java classes from a json schema.'
    group = 'Build'

    project.afterEvaluate {
      configuration = project.jsonSchema2Pojo
      configuration.targetDirectory = configuration.targetDirectory ?:
        project.file("${project.buildDir}/generated-sources/js2p")

      if (project.plugins.hasPlugin('java')) {
        configureJava()
      } else {
        throw new GradleException('generateJsonSchema: Java plugin is required')
      }
      outputs.dir configuration.targetDirectory

      inputs.property("configuration", configuration.toString())
      inputs.files project.files(configuration.sourceFiles)
      outputs.files project.files(configuration.targetDirectory)
    }
  }

  def configureJava() {
    project.sourceSets.main.java.srcDirs += [ configuration.targetDirectory ]
    dependsOn(project.tasks.processResources)
    project.tasks.compileJava.dependsOn(this)

    if (!configuration.source.hasNext()) {
      configuration.source = project.files("${project.sourceSets.main.output.resourcesDir}/json")
      configuration.sourceFiles.each { it.mkdir() }
    }
  }

  @TaskAction
  def generate() {
    logger.info 'Using this configuration:\n{}', configuration
    Jsonschema2Pojo.generate(configuration)
  }
}
