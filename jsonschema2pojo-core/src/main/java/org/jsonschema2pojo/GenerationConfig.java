/**
 * Copyright © 2010-2020 Nokia
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

package org.jsonschema2pojo;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.jsonschema2pojo.rules.RuleFactory;

/**
 * Defines the configuration options for Java type generation, including source
 * and target paths/packages and all behavioural options (e.g should builders be
 * generated, should primitives be used, etc).
 * <p>
 * Devs: add to this interface if you need to introduce a new config property.
 */
public interface GenerationConfig {

  /**
   * Gets the 'generateBuilders' configuration option.
   *
   * @return Whether to generate builder-style methods of the form
   *         <code>withXxx(value)</code> (that return <code>this</code>),
   *         alongside the standard, void-return setters.
   */
  boolean isGenerateBuilders();

  /**
   * Gets the 'includeTypeInfo' configuration option.
   *
   * @return whether to include json type information. Commonly used to support polymorphic type deserialization.
   *
   * @see <a href="Jackson Polymorphic Deserialization">https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization</a>
   *
   */
  boolean isIncludeTypeInfo();

	/**
     * Gets the 'includeConstructorPropertiesAnnotation' configuration option.
     *
     * @return whether to include JDK Constructor Properties. Used by serialization libraries to get parameter names of constructors at runtime. (Not Available on Android)
     *
     * @see <a href="ConstructorProperties">https://docs.oracle.com/javase/7/docs/api/java/beans/ConstructorProperties.html</a>
     */
    boolean isIncludeConstructorPropertiesAnnotation();

    /**
   * Gets the 'usePrimitives' configuration option.
   *
   * @return whether to use primitives (<code>long</code>, <code>double</code>
   *         , <code>boolean</code>) instead of wrapper types where possible
   *         when generating bean properties (has the side-effect of making
   *         those properties non-null).
   */
  boolean isUsePrimitives();

  /**
   * Gets the 'source' configuration option.
   *
   * @return The source file(s) or directory(ies) from which JSON Schema will
   *         be read.
   */
  Iterator<URL> getSource();

  /**
   * Gets the 'targetDirectory' configuration option.
   *
   * @return The target directory into which generated types will be written
   *         (may or may not exist before types are written)
   */
  File getTargetDirectory();

  /**
   * Gets the 'targetPackage' configuration option.
   *
   * @return The java package used for generated types.
   */
  String getTargetPackage();

  /**
   * Gets the 'propertyWordDelimiters' configuration option.
   *
   * @return an array of characters that should act as word delimiters when
   *         choosing java bean property names.
   */
  char[] getPropertyWordDelimiters();

  /**
   * Gets the 'useLongIntegers' configuration option.
   *
   * @return Whether to use the java type <code>long</code> (or
   *         {@link java.lang.Long}) instead of <code>int</code> (or
   *         {@link java.lang.Integer}) when representing the JSON Schema type
   *         'integer'.
   */
  boolean isUseLongIntegers();

  /**
   * Gets the 'useBigIntegers' configuration option.
   *
   * @return Whether to use the java type {@link java.math.BigInteger} instead
   *         of <code>int</code> (or {@link java.lang.Integer}) when
   *         representing the JSON Schema type 'integer'. Note that this
   *         configuration overrides {@link #isUseLongIntegers()}.
   */
  boolean isUseBigIntegers();

  /**
   * Gets the 'useDoubleNumbers' configuration option.
   *
   * @return Whether to use the java type <code>double</code> (or
   *         {@link java.lang.Double}) instead of <code>float</code> (or
   *         {@link java.lang.Float}) when representing the JSON Schema type
   *         'number'.
   */
  boolean isUseDoubleNumbers();

  /**
   * Gets the 'useBigDecimals' configuration option.
   *
   * @return Whether to use the java type {@link java.math.BigDecimal} instead
   *         of <code>float</code> (or {@link java.lang.Float}) when
   *         representing the JSON Schema type 'number'. Note that this
   *         configuration overrides {@link #isUseDoubleNumbers()}.
   */
  boolean isUseBigDecimals();

  /**
   * Gets the 'includeHashcodeAndEquals' configuration option.
   *
   * @return Whether to use include <code>hashCode</code> and
   *         <code>equals</code> methods in generated Java types.
   */
  boolean isIncludeHashcodeAndEquals();

  /**
   * Gets the 'includeToString' configuration option.
   *
   * @return Whether to use include a <code>toString</code> method in
   *         generated Java types.
   */
  boolean isIncludeToString();

  /**
   * Gets the 'toStringExcludes' configuration option.
   *
   * @return An array of strings representing fields
   *         that should be excluded from toString methods
   */
  String[] getToStringExcludes();

  /**
   * Gets the 'annotationStyle' configuration option.
   *
   * @return The style of annotations to use in the generated Java types.
   *         <p>
   *         Supported values:
   *         <ul>
   *         <li><code>jackson1</code> (apply annotations from the
   *         <a href="http://jackson.codehaus.org/">Jackson 1.x</a> library)
   *         </li>
   *         <li><code>jackson2</code> (apply annotations from the
   *         <a href="https://github.com/FasterXML/jackson-annotations">
   *         Jackson 2.x</a> library)</li>
   *         <li><code>gson</code> (apply annotations from the
   *         <a href="https://code.google.com/p/google-gson/">gson</a>
   *         library)</li>
   *         <li><code>moshi1</code> (apply annotations from the
   *         <a href="https://github.com/square/moshi">moshi</a> library)</li>
   *         <li><code>none</code> (apply no annotations at all)</li>
   *         </ul>
   * @see AnnotatorFactory
   */
  AnnotationStyle getAnnotationStyle();

  /**
   * When {@code true} the title is used as class name.
   */
  boolean isUseTitleAsClassname();

  /**
   * Gets the 'inclusionLevel' option for Jackson1 and Jackson2 serializers.
   *
   * @return Level of inclusion to set in the generated Java types.
   *         <p>
   *         Supported values
   *         <ul>
   *         <li><code>ALWAYS</code></li>
   *         <li><code>NON_ABSENT</code></li>
   *         <li><code>NON_DEFAULT</code></li>
   *         <li><code>NON_EMPTY</code></li>
   *         <li><code>NON_NULL</code></li>
   *         <li><code>USE_DEFAULTS</code></li>
   *         </ul>
   *         </p>
   *
   * @see InclusionLevel
   */
  InclusionLevel getInclusionLevel();

  /**
   * Gets the 'customAnnotator' configuration option.
   *
   * @return An annotator that will be used in addition to the one chosen by
   *         {@link #getAnnotationStyle()}
   */
  Class<? extends Annotator> getCustomAnnotator();

  /**
   * Gets the 'customRuleFactory' configuration option.
   *
   * @return An Rule Factory that will be used for the creation of generation
   *         rules.
   */
  Class<? extends RuleFactory> getCustomRuleFactory();

  /**
   * Gets the 'includeJsr303Annotations' configuration option.
   *
   * @return Whether to include
   *         <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a>
   *         annotations (for schema rules like minimum, maximum, etc) in
   *         generated Java types.
   */
  boolean isIncludeJsr303Annotations();

  /**
   * Gets the 'includeJsr305Annotations' configuration option.
   *
   * @return Whether to include
   *         <a href="http://jcp.org/en/jsr/detail?id=305">JSR-305</a>
   *         annotations (for schema rules like Nullable, NonNull, etc) in
   *         generated Java types.
   */
  boolean isIncludeJsr305Annotations();

  /**
   * Gets the 'useOptionalForGetters' configuration option.
   *
   * @return Whether to use {@link java.util.Optional} as return type for
   *         getters of non-required fields.
   */
  boolean isUseOptionalForGetters();

  /**
   * Gets the 'sourceType' configuration option.
   *
   * @return The type of input documents that will be read
   *         <p>
   *         Supported values:
   *         <ul>
   *         <li><code>jsonschema</code></li>
   *         <li><code>json</code></li>
   *         </ul>
   */
  SourceType getSourceType();

  /**
   * Gets the 'removeOldOutput' configuration option.
   *
   * @return Whether to empty the target directory before generation occurs,
   *         to clear out all source files that have been generated
   *         previously. <strong>Be warned</strong>, when activated this
   *         option will cause jsonschema2pojo to <strong>indiscriminately
   *         delete the entire contents of the target directory (all files and
   *         folders)</strong> before it begins generating sources.
   */
  boolean isRemoveOldOutput();

  /**
   * Gets the 'outputEncoding' configuration option.
   *
   * @return The character encoding that should be used when writing the
   *         generated Java source files.
   */
  String getOutputEncoding();

  /**
   * Gets the 'useJodaDates' configuration option.
   *
   * @return Whether to use {@link org.joda.time.DateTime} instead of
   *         {@link java.util.Date} when adding date type fields to generated
   *         Java types.
   */
  boolean isUseJodaDates();

  /**
   * Gets the 'useJodaLocalDates' configuration option.
   *
   * @return Whether to use {@link org.joda.time.LocalDate} instead of string
   *         when adding string type fields with a format of date (not
   *         date-time) to generated Java types.
   */
  boolean isUseJodaLocalDates();

  /**
   * Gets the 'useJodaLocalTimes' configuration option.
   *
   * @return Whether to use {@link org.joda.time.LocalTime} instead of string
   *         when adding string type fields with a format of time (not
   *         date-time) to generated Java types.
   */
  boolean isUseJodaLocalTimes();

  /**
   * Gets the 'parcelable' configuration option.
   *
   * @return Whether to make the generated types 'parcelable' (for Android
   *         development)
   */
  boolean isParcelable();

  /**
   * Gets the 'serializable' configuration option.
   *
   * @return Whether to make the generated types 'serializable'
   */
  boolean isSerializable();

  /**
   * Gets the file filter used to isolate the schema mapping files in the
   * source directories.
   *
   * @return the file filter use when scanning for schema files.
   */
  FileFilter getFileFilter();

  /**
   * Gets the 'initializeCollections' configuration option.
   *
   * @return Whether to initialize collections with empty instance or null.
   */
  boolean isInitializeCollections();

  /**
   * Gets the 'getClassNamePrefix' configuration option.
   *
   * @return Whether to add a prefix to generated classes.
   */
  String getClassNamePrefix();

  /**
   * Gets the 'getClassNameSuffix' configuration option.
   *
   * @return Whether to add a suffix to generated classes.
   */
  String getClassNameSuffix();

  /**
   * Gets the 'fileExtensions' configuration option.
   *
   * @return An array of strings that should be considered as file extensions
   *         and therefore not included in class names.
   */
  String[] getFileExtensions();

  /**
   * Gets the 'includeConstructors' configuration option.
   *
   * @return Whether to generate constructors or not.
   */
  boolean isIncludeConstructors();

  /**
   * Gets the 'constructorsRequiredPropertiesOnly' configuration option. This is a legacy configuration option used to turn on the {@link
   * #isIncludeAllPropertiesConstructor()} and off the {@link #isIncludeAllPropertiesConstructor()} configuration options.
   * It is specifically tied to the {@link #isIncludeConstructors()} property, and will do nothing if that property is not enabled
   */
  boolean isConstructorsRequiredPropertiesOnly();

  /**
   * Gets the 'constructorsIncludeRequiredPropertiesConstructor' configuration option. This property works in collaboration with the {@link
   * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
   * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
   * whether the resulting object should include a constructor with only the required properties as parameters.
   */
  boolean isIncludeRequiredPropertiesConstructor();

  /**
   * Gets the 'constructorsIncludeRequiredPropertiesConstructor' configuration option. This property works in collaboration with the {@link
   * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
   * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
   * whether the resulting object should include a constructor with all listed properties as parameters.
   */
  boolean isIncludeAllPropertiesConstructor();

  /**
   * Gets the 'constructorsIncludeRequiredPropertiesConstructor' configuration option. This property works in collaboration with the {@link
   * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
   * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
   * whether the resulting object should include a constructor the class itself as a parameter, with the expectation that all properties from the
   * originating class will assigned to the new class.
   */
  boolean isIncludeCopyConstructor();

  /**
   * Gets the 'includeAdditionalProperties' configuration option.
   *
   * @return Whether to allow 'additional properties' support in objects.
   *         Setting this to false will disable additional properties support,
   *         regardless of the input schema(s).
   */
  boolean isIncludeAdditionalProperties();

  /**
   * Gets the 'includeGetters' configuration option.
   *
   * @return Whether to include getters or to omit these accessor
   *         methods and create public fields instead.
   */
  boolean isIncludeGetters();

  /**
   * Gets the 'includeSetters' configuration option.
   *
   * @return Whether to include setters or to omit these accessor
   *         methods and create public fields instead.
   */
  boolean isIncludeSetters();

  /**
   * Gets the 'targetVersion' configuration option.
   *
   * @return The target version for generated source files.
   */
  String getTargetVersion();

  /**
   * Gets the `includeDynamicAccessors` configuration option
   *
   * @return Whether to include dynamic getters, setters, and builders or to
   *         omit these methods.
   */
  boolean isIncludeDynamicAccessors();

  /**
   * Gets the `includeDynamicGetters` configuration option.
   *
   * @return Whether to include dynamic getters or to omit these methods
   */
  boolean isIncludeDynamicGetters();

  /**
   * Gets the `includeDynamicSetters` configuration option.
   *
   * @return Whether to include dynamic setters or to omit these methods
   */
  boolean isIncludeDynamicSetters();

  /**
   * Gets the `includeDynamicBuilders` configuration option.
   *
   * @return Whether to include dynamic builders or to omit these methods
   */
  boolean isIncludeDynamicBuilders();

  /**
   * Gets the `dateTimeType` configuration option.
   * <p>
   * Example values:
   * <ul>
   * <li><code>org.joda.time.LocalDateTime</code> (Joda)</li>
   * <li><code>java.time.LocalDateTime</code> (JSR310)</li>
   * <li><code>null</code> (default behavior)</li>
   * </ul>
   *
   * @return The java type to use instead of {@link java.util.Date} when
   *         adding date type fields to generate Java types.
   */
  String getDateTimeType();

  /**
   * Gets the `dateType` configuration option.
   * <p>
   * Example values:
   * <ul>
   * <li><code>org.joda.time.LocalDate</code> (Joda)</li>
   * <li><code>java.time.LocalDate</code> (JSR310)</li>
   * <li><code>null</code> (default behavior)</li>
   * </ul>
   *
   * @return The java type to use instead of string when adding string type
   *         fields with a format of date (not date-time) to generated Java
   *         types.
   */
  String getDateType();

  /**
   * Gets the `timeType` configuration option.
   * <p>
   * Example values:
   * <ul>
   * <li><code>org.joda.time.LocalTime</code> (Joda)</li>
   * <li><code>java.time.LocalTime</code> (JSR310)</li>
   * <li><code>null</code> (default behavior)</li>
   * </ul>
   *
   * @return The java type to use instead of string when adding string type
   *         fields with a format of time (not date-time) to generated Java
   *         types.
   */
  String getTimeType();

  /**
   * Gets the `formatDates` configuration option
   *
   * @return Whether the fields of type <code>date</code> have the
   *         <code>@JsonFormat</code> annotation with pattern set to the
   *         default value of <code>yyyy-MM-dd</code>
   */
  boolean isFormatDates();

  /**
   * Gets the `formatTimes` configuration option
   *
   * @return Whether the fields of type <code>time</code> have the
   *         <code>@JsonFormat</code> annotation with pattern set to the
   *         default value of <code>HH:mm:ss.SSS</code>
   */
  boolean isFormatTimes();

  /**
   * Gets the `formatDateTime` configuration option
   *
   * @return Whether the fields of type <code>date-type</code> have the
   *         <code>@JsonFormat</code> annotation with pattern set to the
   *         default value of <code>yyyy-MM-dd'T'HH:mm:ss.SSSZ</code>
   */
  boolean isFormatDateTimes();

  /**
   * Gets the 'customDatePattern' configuration option
   *
   * @return The custom format that dates will use when types are serialized.
   *         Requires support from your JSON binding library.
   */
  String getCustomDatePattern();

  /**
   * Gets the 'customTimePattern' configuration option
   *
   * @return The custom format that times will use when types are serialized.
   *         Requires support from your JSON binding library.
   */
  String getCustomTimePattern();

  /**
   * Gets the 'customDateTimePattern' configuration option
   *
   * @return The custom format that dates will use when types are serialized.
   *         Requires support from your JSON binding library.
   */
  String getCustomDateTimePattern();

  /**
   * Gets the `refFragmentPathDelimiters` configuration option.
   *
   * @return A string containing any characters that should act as path
   *         delimiters when resolving $ref fragments. By default, #, / and .
   *         are used in an attempt to support JSON Pointer and JSON Path.
   */
  String getRefFragmentPathDelimiters();

  /**
   * Gets the 'sourceSortOrder' configuration option.
   *
   * @return
   *  <p>
   *         Supported values:
   *         <ul>
   *         <li><code>OS</code> (Let the OS influence the order the source files are processed.)</li>
   *         <li><code>FILES_FIRST</code> (Case sensitive sort, visit the files first.  The source files are processed in a breadth
   *         first sort order.)</li>
   *         <li><code>SUBDIRS_FIRST</code> (Case sensitive sort, visit the sub-directories before the files.  The source files are
   *         processed in a depth first sort order.)</li>
   *         </ul>
   */
  SourceSortOrder getSourceSortOrder();

  /**
   * Gets the 'targetLanguage' configuration option.
   *
   * @return The type of code that will be generated.
   *         <p>
   *         Supported values:
   *         <ul>
   *         <li><code>JAVA</code> (Generate .java source files)</li>
   *         <li><code>SCALA</code> (Generate .scala source files, using scalagen)</li>
   *         </ul>
   */
  Language getTargetLanguage();

  /**
   * Gets the 'formatTypeMapping' configuration option.
   *
   * @return An optional mapping from format identifier (e.g. 'uri') to
   *         fully qualified type name (e.g. 'java.net.URI').
   */
  Map<String, String> getFormatTypeMapping();

  /**
   * If set to true, then the gang of four builder pattern will be used to generate builders on generated classes. Note: This property works
   * in collaboration with the {@link #isGenerateBuilders()} method. If the {@link #isGenerateBuilders()} is false,
   * then this property will not do anything.
   * @return whether to include the gang of four builder patter on the generated classes. The default value for this is false.
   */
  default boolean isUseInnerClassBuilders() {
    return false;
  }

}
