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

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.InputChanges

/**
 * Task that generates java source files for an Android project
 */
class GenerateJsonSchemaAndroidTask extends SourceTask {

  /**
   * The output directory.
   */
  @OutputDirectory
  File outputDir

  @TaskAction
  def generate(InputChanges inputs) {

    // If the whole thing isn't incremental, delete the build folder (if it exists)
    if (!inputs.isIncremental() && outputDir.exists()) {
      logger.debug("JsonSchema2Pojo generation is not incremental; deleting build folder and starting fresh!")
      outputDir.deleteDir()
    }

    if (!outputDir.exists()) {
      outputDir.mkdirs()
    }

    GenerationConfig configuration = project.jsonSchema2Pojo
    configuration.targetDirectory = outputDir
    setTargetVersion configuration

    if (Boolean.TRUE == configuration.properties.get("useCommonsLang3")) {
      logger.warn 'useCommonsLang3 is deprecated. Please remove it from your config.'
    }

    logger.info 'Using this configuration:\n{}', configuration

    Jsonschema2Pojo.generate(configuration, new GradleRuleLogger(logger))
  }

  void setTargetVersion(JsonSchemaExtension configuration) {
    if (!configuration.targetVersion && (project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.library"))) {
        configuration.targetVersion = project.android.compileOptions.sourceCompatibility
        logger.info 'Using android.compileOptions.sourceCompatibility as targetVersion for jsonschema2pojo: ' + configuration.targetVersion
    }
  }

}
