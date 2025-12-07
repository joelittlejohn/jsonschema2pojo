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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the plugin's tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
class JsonSchemaPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.extensions.create('jsonSchema2Pojo', JsonSchemaExtension)

    if (project.plugins.hasPlugin('java')) {
      project.tasks.create('generateJsonSchema2Pojo', GenerateJsonSchemaJavaTask)
    } else if (project.plugins.hasPlugin('com.android.application') || project.plugins.hasPlugin('com.android.library')) {
      def config = project.jsonSchema2Pojo
      def variants
      if (project.android.hasProperty('applicationVariants')) {
        variants = project.android.applicationVariants
      } else if (project.android.hasProperty('libraryVariants')) {
        variants = project.android.libraryVariants
      } else {
        throw new IllegalStateException('Android project must have applicationVariants or libraryVariants!')
      }

      variants.all { variant ->

        GenerateJsonSchemaAndroidTask task = (GenerateJsonSchemaAndroidTask) project.task(type: GenerateJsonSchemaAndroidTask, "generateJsonSchema2PojoFor${variant.name.capitalize()}") {
          source = config.source.collect { it }
          outputDir = project.layout.buildDirectory.dir("generated/source/js2p/$variant.flavorName/$variant.buildType.name/").get().asFile
        }

        variant.registerJavaGeneratingTask(task, (File) task.outputDir)
      }
    } else {
      throw new GradleException('generateJsonSchema: Java or Android plugin required')
    }
  }
}
