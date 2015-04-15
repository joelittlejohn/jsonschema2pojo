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

import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.Annotator
import org.jsonschema2pojo.AllFileFilter
import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.NoopAnnotator
import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.rules.RuleFactory

/**
 * The configuration properties.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 * @see https://github.com/joelittlejohn/jsonschema2pojo
 */
public class JsonSchemaExtension implements GenerationConfig {
  boolean generateBuilders
  boolean usePrimitives
  Iterable<File> sourceFiles
  File targetDirectory
  String targetPackage
  char[] propertyWordDelimiters
  boolean useLongIntegers
  boolean useDoubleNumbers
  boolean includeConstructors
  boolean constructorsRequiredPropertiesOnly
  boolean includeHashcodeAndEquals
  boolean includeToString
  AnnotationStyle annotationStyle
  Class<? extends Annotator> customAnnotator
  Class<? extends RuleFactory> customRuleFactory
  boolean includeJsr303Annotations
  SourceType sourceType
  boolean removeOldOutput
  String outputEncoding
  boolean useJodaDates
  boolean useJodaLocalDates
  boolean useJodaLocalTimes
  boolean useCommonsLang3
  boolean parcelable
  FileFilter fileFilter
  boolean initializeCollections
  String classNamePrefix
  String classNameSuffix

  public JsonSchemaExtension() {
    // See DefaultGenerationConfig
    generateBuilders = false
    usePrimitives = false
    sourceFiles = []
    targetPackage = ''
    propertyWordDelimiters = [] as char[]
    useLongIntegers = false
    useDoubleNumbers = true
    includeHashcodeAndEquals = true
    includeConstructors = false
    constructorsRequiredPropertiesOnly = false
    includeToString = true
    annotationStyle = AnnotationStyle.JACKSON
    customAnnotator = NoopAnnotator.class
    customRuleFactory = RuleFactory.class
    includeJsr303Annotations = false
    sourceType = SourceType.JSONSCHEMA
    outputEncoding = 'UTF-8'
    useJodaDates = false
    useJodaLocalDates = false
    useJodaLocalTimes = false
    useCommonsLang3 = false
    parcelable = false
    fileFilter = new AllFileFilter()
    initializeCollections = true
    classNamePrefix = ''
    classNameSuffix = ''
  }

  @Override
  public Iterator<URL> getSource() {
    def urlList = []
    for (source in sourceFiles) {
      urlList.add(source.toURI().toURL())
    }
    urlList.iterator()
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

  public void setCustomRuleFactory(String clazz) {
    customRuleFactory = Class.forName(clazz)
  }

  public void setSourceType(String s) {
    sourceType = SourceType.valueOf(s.toUpperCase())
  }
  
  public void setClassNamePrefix(String s) {
    classNamePrefix = s
  }
  
  public String getClassNamePrefix() {
    classNamePrefix
  }
  
  public void setClassNameSuffix(String s) {
    classNameSuffix = s
  }
  
  public String getClassNameSuffix() {
    classNameSuffix
  }

    /**
     * Gets the 'includeConstructors' configuration option
     *
     * @return Whether to generate constructors or not.
     */
    @Override
    boolean isIncludeConstructors() {
        includeConstructors;
    }

    /**
     * Gets the 'constructorsRequiredPropertiesOnly' configuration option
     *
     * @return Whether generated constructors should have parameters for all properties, or only required ones.
     */
    @Override
    boolean isConstructorsRequiredPropertiesOnly() {
        constructorsRequiredPropertiesOnly
    }

    @Override
  public String toString() {
    """|generateBuilders = ${generateBuilders}
       |usePrimitives = ${usePrimitives}
       |source = ${sourceFiles}
       |targetDirectory = ${targetDirectory}
       |targetPackage = ${targetPackage}
       |propertyWordDelimiters = ${Arrays.toString(propertyWordDelimiters)}
       |useLongIntegers = ${useLongIntegers}
       |useDoubleNumbers = ${useDoubleNumbers}
       |includeHashcodeAndEquals = ${includeHashcodeAndEquals}
       |includeConstructors = ${includeConstructors}
       |includeToString = ${includeToString}
       |annotationStyle = ${annotationStyle.toString().toLowerCase()}
       |customAnnotator = ${customAnnotator.getName()}
       |customRuleFactory = ${customRuleFactory.getName()}
       |includeJsr303Annotations = ${includeJsr303Annotations}
       |sourceType = ${sourceType.toString().toLowerCase()}
       |removeOldOutput = ${removeOldOutput}
       |outputEncoding = ${outputEncoding}
       |useJodaDates = ${useJodaDates}
       |useJodaLocalDates = ${useJodaLocalDates}
       |useJodaLocalTimes = ${useJodaLocalTimes}
       |useCommonsLang3 = ${useCommonsLang3}
       |parcelable = ${parcelable}
       |initializeCollections = ${initializeCollections}
       |classNamePrefix = ${classNamePrefix}
       |classNameSuffix = ${classNameSuffix}
     """.stripMargin()
  }
}
