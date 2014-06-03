/**
 * Copyright © 2010-2013 Nokia
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

import java.util.Map
import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.Annotator
import org.jsonschema2pojo.AllFileFilter
import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.NoopAnnotator
import org.jsonschema2pojo.SourceType

/**
 * The configuration properties.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 * @see https://github.com/joelittlejohn/jsonschema2pojo
 */
public class JsonSchemaExtension implements GenerationConfig {
  boolean generateBuilders
  boolean generateBuilderClasses
  boolean immutable
  boolean usePublicFields
  boolean usePrimitives
  Iterable<File> sourceFiles
  File targetDirectory
  String targetPackage
  char[] propertyWordDelimiters
  boolean useLongIntegers
  boolean useDoubleNumbers
  boolean includeHashcodeAndEquals
  boolean includeToString
  AnnotationStyle annotationStyle
  Class<? extends Annotator> customAnnotator
  boolean includeJsr303Annotations
  SourceType sourceType
  boolean removeOldOutput
  String outputEncoding
  boolean useJodaDates
  boolean useCommonsLang3
  FileFilter fileFilter

  public JsonSchemaExtension() {
    // See DefaultGenerationConfig
    generateBuilders = false
    generateBuilderClasses = false
    immutable = false
    usePublicFields = false
    usePrimitives = false
    sourceFiles = []
    targetPackage = ''
    propertyWordDelimiters = [] as char[]
    useLongIntegers = false
    useDoubleNumbers = true
    includeHashcodeAndEquals = true
    includeToString = true
    annotationStyle = AnnotationStyle.JACKSON
    customAnnotator = NoopAnnotator.class
    includeJsr303Annotations = false
    sourceType = SourceType.JSONSCHEMA
    outputEncoding = 'UTF-8'
    useJodaDates = false
    useCommonsLang3 = false
    fileFilter = new AllFileFilter()
  }

  @Override
  public Iterator<File> getSource() {
    sourceFiles.iterator()
  }

  public void setSource(Iterable<File> files) {
    def copy = [] as List
    files.each { copy.add(it) }
    sourceFiles = copy
  }

  public void setAnnotationStyle(String style) {
    annotationStyle = AnnotationStyle.valueOf(style.toUpperCase())
  }

  public void setCustomAnnotator(String clazz) {
    customAnnotator = Class.forName(clazz)
  }

  public void setSourceType(String s) {
    sourceType = SourceType.valueOf(s.toUpperCase())
  }

  @Override
  public String toString() {
    """|generateBuilders = ${generateBuilders}
       |generateBuilderClasses = ${generateBuilderClasses}
       |immutable = ${immutable}
       |usePublicFields = ${usePublicFields}
       |usePrimitives = ${usePrimitives}
       |source = ${sourceFiles}
       |targetDirectory = ${targetDirectory}
       |targetPackage = ${targetPackage}
       |propertyWordDelimiters = ${Arrays.toString(propertyWordDelimiters)}
       |useLongIntegers = ${useLongIntegers}
       |useDoubleNumbers = ${useDoubleNumbers}
       |includeHashcodeAndEquals = ${includeHashcodeAndEquals}
       |includeToString = ${includeToString}
       |annotationStyle = ${annotationStyle.toString().toLowerCase()}
       |customAnnotator = ${customAnnotator.getName()}
       |includeJsr303Annotations = ${includeJsr303Annotations}
       |sourceType = ${sourceType.toString().toLowerCase()}
       |removeOldOutput = ${removeOldOutput}
       |outputEncoding = ${outputEncoding}
       |useJodaDates = ${useJodaDates}
       |useCommonsLang3 = ${useCommonsLang3}
     """.stripMargin()
  }
}
