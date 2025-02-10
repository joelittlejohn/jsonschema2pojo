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

import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.Jsonschema2Pojo

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.model.ReplacedBy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * A task that performs code generation.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
class GenerateJsonSchemaJavaTask extends DefaultTask {
  @ReplacedBy("configurationString")
  GenerationConfig configuration

  @Input
  String getConfigurationString() {
    configuration.toString();
  }

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
      setTargetVersion configuration

      inputs.property("configuration", configuration.toString())
      inputs.files project.files(configuration.source.findAll { 'file'.equals(it.protocol) })
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
    if (Boolean.TRUE == configuration.properties.get("useCommonsLang3")) {
      logger.warn 'useCommonsLang3 is deprecated. Please remove it from your config.'
    }

    logger.info 'Using this configuration:\n{}', configuration

    Jsonschema2Pojo.generate(configuration, new GradleRuleLogger(logger))
  }

  void setTargetVersion(JsonSchemaExtension configuration) {
    if (!configuration.targetVersion) {
      def compileJavaTask = project.getTasksByName("compileJava", false).first()
      configuration.targetVersion = compileJavaTask.getProperties().get("sourceCompatibility")
      logger.info 'Using Gradle sourceCompatibility as targetVersion for jsonschema2pojo: ' + configuration.targetVersion
    }
  }
}
