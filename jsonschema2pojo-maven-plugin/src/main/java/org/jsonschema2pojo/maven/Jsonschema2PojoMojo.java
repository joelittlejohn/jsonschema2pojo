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

package org.jsonschema2pojo.maven;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.jsonschema2pojo.AllFileFilter;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.AnnotatorFactory;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.InclusionLevel;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.SourceSortOrder;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.JavaVersion;
import org.jsonschema2pojo.util.URLUtil;

/**
 * When invoked, this goal reads one or more
 * <a href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 *
 * @see <a href="http://maven.apache.org/developers/mojo-api-specification.html">Mojo API Specification</a>
 */
@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class Jsonschema2PojoMojo extends AbstractMojo implements GenerationConfig {

    /**
     * Target directory for generated Java source files.
     *
     * @since 0.1.0
     */
    @Parameter(property = "jsonschema2pojo.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/jsonschema2pojo")
    private File outputDirectory;

    /**
     * Location of the JSON Schema file(s). Note: this may refer to a single
     * file or a directory of files.
     *
     * @since 0.1.0
     */
    @Parameter(property = "jsonschema2pojo.sourceDirectory")
    private String sourceDirectory;

    /**
     * An array of locations of the JSON Schema file(s). Note: each item may
     * refer to a single file or a directory of files.
     *
     * @since 0.3.1
     */
    @Parameter(property = "jsonschema2pojo.sourcePaths")
    private String[] sourcePaths;

    /**
     * Package name used for generated Java classes (for types where a fully
     * qualified name has not been supplied in the schema using the 'javaType'
     * property).
     *
     * @since 0.1.0
     */
    @Parameter(property = "jsonschema2pojo.targetPackage")
    private String targetPackage = "";

    /**
     * Whether to generate builder-style methods of the form
     * <code>withXxx(value)</code> (that return <code>this</code>), alongside
     * the standard, void-return setters.
     *
     * @since 0.1.2
     */
    @Parameter(property = "jsonschema2pojo.generateBuilders", defaultValue="false")
    private boolean generateBuilders = false;

    /**
     * Whether to include json type information; often required to support polymorphic type handling.
     * <p>
     * By default the type information is stored in the @class property, this can be overridden in the deserializationClassProperty
     * of the schema.
     *
     * @since 1.0.2
     */
    @Parameter(property = "jsonschema2pojo.includeTypeInfo", defaultValue = "false")
    private boolean includeTypeInfo = false;
    /**
     * Whether to use primitives (<code>long</code>, <code>double</code>,
     * <code>boolean</code>) instead of wrapper types where possible when
     * generating bean properties (has the side-effect of making those
     * properties non-null).
     *
     * @since 0.2.0
     */
    @Parameter(property = "jsonschema2pojo.usePrimitives", defaultValue = "false")
    private boolean usePrimitives = false;

    /**
     * Add the output directory to the project as a source root, so that the
     * generated java types are compiled and included in the project artifact.
     *
     * @since 0.1.9
     */
    @Parameter(property = "jsonschema2pojo.addCompileSourceRoot", defaultValue = "true")
    private boolean addCompileSourceRoot = true;

    /**
     * Skip plugin execution (don't read/validate any schema files, don't
     * generate any java types).
     *
     * @since 0.2.1
     */
    @Parameter(property = "jsonschema2pojo.skip", defaultValue = "false")
    private boolean skip = false;

    /**
     * The characters that should be considered as word delimiters when creating
     * Java Bean property names from JSON property names. If blank or not set,
     * JSON properties will be considered to contain a single word when creating
     * Java Bean property names.
     *
     * @since 0.2.2
     */
    @Parameter(property = "jsonschema2pojo.propertyWordDelimiters", defaultValue = "- _")
    private String propertyWordDelimiters = "- _";

    /**
     * Whether to use the java type <code>long</code> (or <code>Long</code>)
     * instead of <code>int</code> (or <code>Integer</code>) when representing
     * the JSON Schema type 'integer'.
     *
     * @since 0.2.2
     */
    @Parameter(property = "jsonschema2pojo.useLongIntegers", defaultValue = "false")
    private boolean useLongIntegers = false;

    /**
     * Whether to use the java type {@link java.math.BigInteger} instead of
     * <code>int</code> (or {@link java.lang.Integer}) when representing the
     * JSON Schema type 'integer'. Note that this configuration overrides
     * {@link #isUseLongIntegers()}.
     *
     * @since 0.4.25
     */
    @Parameter(property = "jsonschema2pojo.useBigIntegers", defaultValue = "false")
    private boolean useBigIntegers = false;

    /**
     * Whether to use the java type <code>double</code> (or <code>Double</code>)
     * instead of <code>float</code> (or <code>Float</code>) when representing
     * the JSON Schema type 'number'.
     *
     * @since 0.4.0
     */
    @Parameter(property = "jsonschema2pojo.useDoubleNumbers", defaultValue = "true")
    private boolean useDoubleNumbers = true;

    /**
     * Whether to use the java type {@link java.math.BigDecimal} instead of
     * <code>float</code> (or {@link java.lang.Float}) when representing the
     * JSON Schema type 'number'. Note that this configuration overrides
     * {@link #isUseDoubleNumbers()}.
     *
     * @since 0.4.22
     */
    @Parameter(property = "jsonschema2pojo.useBigDecimals", defaultValue = "false")
    private boolean useBigDecimals = false;

    /**
     * Whether to include <code>hashCode</code> and <code>equals</code> methods
     * in generated Java types.
     *
     * @since 0.3.1
     */
    @Parameter(property = "jsonschema2pojo.includeHashcodeAndEquals", defaultValue = "true")
    private boolean includeHashcodeAndEquals = true;

    /**
     * Whether to include a <code>toString</code> method in generated Java
     * types.
     *
     * @since 0.3.1
     */
    @Parameter(property = "jsonschema2pojo.includeToString", defaultValue = "true")
    private boolean includeToString = true;

    /**
     * The fields to be excluded from toString generation
     *
     * @since 0.4.35
     */
    @Parameter(property = "jsonschema2pojo.toStringExcludes", defaultValue = "")
    private String[] toStringExcludes = new String[] {};

    /**
     * The style of annotations to use in the generated Java types.
     * <p>
     * Supported values:
     * <ul>
     * <li><code>jackson2</code> (apply annotations from the
     * <a href="https://github.com/FasterXML/jackson-annotations">Jackson
     * 2.x</a> library)</li>
     * <li><code>jackson3</code> (apply annotations from the
     * <a href="https://github.com/FasterXML/jackson-annotations">Jackson
     * 2.x</a> library, and from <a href="https://github.com/FasterXML/jackson-databind">Jackson 3.x databind</a>
     * for data-binding annotations)</li>
     * <li><code>jackson</code> (alias for jackson2)</li>
     * <li><code>jsonb</code> (apply annotations from the
     * JSON-B 1.x library)</li>
     * <li><code>jsonb2</code> (apply annotations from the
     * JSON-B 2.x library)</li>
     * <li><code>gson</code> (apply annotations from the
     * <a href="https://code.google.com/p/google-gson/">gson</a> library)</li>
     * <li><code>moshi1</code> (apply annotations from the
     * <a href="https://github.com/square/moshi">moshi 1.x</a> library)</li>
     * <li><code>none</code> (apply no annotations at all)</li>
     * </ul>
     *
     * @since 0.3.1
     */
    @Parameter(property = "jsonschema2pojo.annotationStyle", defaultValue = "jackson2")
    private String annotationStyle = "jackson2";

    /**
     * Use the title as class name. Otherwise, the property and file name is used.
     *
     * @since 1.0.0
     */
    @Parameter(property = "jsonschema2pojo.useTitleAsClassname", defaultValue = "false")
    private boolean useTitleAsClassname = false;

    /**
     * The Level of inclusion to set in the generated Java types for
     * Jackson serializers.
     * <p>
     * Supported values
     * <ul>
     * <li><code>ALWAYS</code></li>
     * <li><code>NON_ABSENT</code></li>
     * <li><code>NON_DEFAULT</code></li>
     * <li><code>NON_EMPTY</code></li>
     * <li><code>NON_NULL</code></li>
     * <li><code>USE_DEFAULTS</code></li>
     * </ul>
     * </p>
     *
     */
    @Parameter(property = "jsonschema2pojo.inclusionLevel", defaultValue = "NON_NULL")
    private String inclusionLevel = "NON_NULL";

    /**
     * A fully qualified class name, referring to a custom annotator class that
     * implements <code>org.jsonschema2pojo.Annotator</code> and will be used in
     * addition to the one chosen by <code>annotationStyle</code>.
     * <p>
     * If you want to use the custom annotator alone, set
     * <code>annotationStyle</code> to <code>none</code>.
     *
     * @since 0.3.6
     */
    @Parameter(property = "jsonschema2pojo.customAnnotator", defaultValue = "org.jsonschema2pojo.NoopAnnotator")
    private String customAnnotator = NoopAnnotator.class.getName();

    /**
     * A fully qualified class name, referring to an class that extends
     * <code>org.jsonschema2pojo.rules.RuleFactory</code> and will be used to
     * create instances of Rules used for code generation.
     *
     * @since 0.4.5
     */
    @Parameter(property = "jsonschema2pojo.customRuleFactory", defaultValue = "org.jsonschema2pojo.rules.RuleFactory")
    private String customRuleFactory = RuleFactory.class.getName();

    /**
     * Whether to include
     * <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303/349</a> annotations
     * (for schema rules like minimum, maximum, etc) in generated Java types.
     * <p>
     * Schema rules and the annotation they produce:
     * <ul>
     * <li>maximum = {@literal @DecimalMax}
     * <li>minimum = {@literal @DecimalMin}
     * <li>minItems,maxItems = {@literal @Size}
     * <li>minLength,maxLength = {@literal @Size}
     * <li>pattern = {@literal @Pattern}
     * <li>required = {@literal @NotNull}
     * </ul>
     * Any Java fields which are an object or array of objects will be annotated
     * with {@literal @Valid} to support validation of an entire document tree.
     *
     * @since 0.3.2
     */
    @Parameter(property = "jsonschema2pojo.includeJsr303Annotations", defaultValue = "false")
    private boolean includeJsr303Annotations = false;

    /**
     * Whether to include
     * <a href="http://jcp.org/en/jsr/detail?id=305">JSR-305</a> annotations
     * (for schema rules like Nullable, NonNull, etc) in generated Java types.
     *
     * @since 0.4.8
     */
    @Parameter(property = "jsonschema2pojo.includeJsr305Annotations", defaultValue = "false")
    private boolean includeJsr305Annotations = false;

    /**
     * Whether to use {@link java.util.Optional} as return type for
     * getters of non-required fields.
     *
     */
    @Parameter(property = "jsonschema2pojo.useOptionalForGetters", defaultValue = "false")
    private boolean useOptionalForGetters = false;

    /**
     * The type of input documents that will be read
     * <p>
     * Supported values:
     * <ul>
     * <li><code>jsonschema</code> (schema documents, containing formal rules
     * that describe the structure of JSON data)</li>
     * <li><code>json</code> (documents that represent an example of the kind of
     * JSON data that the generated Java types will be mapped to)</li>
     * <li><code>yamlschema</code> (JSON schema documents, represented as YAML)</li>
     * <li><code>yaml</code> (documents that represent an example of the kind of
     * YAML (or JSON) data that the generated Java types will be mapped to)</li>
     * </ul>
     *
     * @since 0.3.3
     */
    @Parameter(property = "jsonschema2pojo.sourceType", defaultValue = "jsonschema")
    private String sourceType = "jsonschema";

    /**
     * Whether to empty the target directory before generation occurs, to clear
     * out all source files that have been generated previously.
     * <p>
     * <strong>Be warned</strong>, when activated this option will cause
     * jsonschema2pojo to <strong>indiscriminately delete the entire contents of
     * the target directory (all files and folders)</strong> before it begins
     * generating sources.
     *
     * @since 0.3.7
     */
    @Parameter(property = "jsonschema2pojo.removeOldOutput", defaultValue = "false")
    private boolean removeOldOutput = false;

    /**
     * The character encoding that should be used when writing the generated
     * Java source files.
     *
     * @since 0.4.0
     */
    @Parameter(property = "jsonschema2pojo.outputEncoding", defaultValue = "UTF-8")
    private String outputEncoding = "UTF-8";

    /**
     * Whether to use {@link org.joda.time.DateTime} instead of
     * {@link java.util.Date} when adding date type fields to generated Java
     * types.
     *
     * @since 0.4.0
     */
    @Parameter(property = "jsonschema2pojo.useJodaDates", defaultValue = "false")
    private boolean useJodaDates = false;

    /**
     * Whether to use {@link org.joda.time.LocalDate} instead of string when
     * adding string type fields of format date (not date-time) to generated
     * Java types.
     *
     * @since 0.4.9
     */
    @Parameter(property = "jsonschema2pojo.useJodaLocalDates", defaultValue = "false")
    private boolean useJodaLocalDates = false;

    /**
     * Whether to use {@link org.joda.time.LocalTime} instead of string when
     * adding string type fields of format time (not date-time) to generated
     * Java types.
     *
     * @since 0.4.9
     */
    @Parameter(property = "jsonschema2pojo.useJodaLocalTimes", defaultValue = "false")
    private boolean useJodaLocalTimes = false;

    /**
     * What type to use instead of string when adding string type fields of
     * format date-time to generated Java types.
     *
     * @since 0.4.22
     */
    @Parameter(property = "jsonschema2pojo.dateTimeType")
    private String dateTimeType = null;

    /**
     * What type to use instead of string when adding string type fields of
     * format time (not date-time) to generated Java types.
     *
     * @since 0.4.22
     */
    @Parameter(property = "jsonschema2pojo.timeType")
    private String timeType = null;

    /**
     * What type to use instead of string when adding string type fields of
     * format date (not date-time) to generated Java types.
     *
     * @since 0.4.22
     */
    @Parameter(property = "jsonschema2pojo.dateType")
    private String dateType = null;

    /**
     * Whether to make the generated types 'parcelable' (for Android development).
     *
     * @since 0.4.11
     */
    @Parameter(property = "jsonschema2pojo.parcelable", defaultValue = "false")
    private boolean parcelable = false;

    /**
     * Whether to make the generated types 'serializable'.
     *
     * @since 0.4.23
     */
    @Parameter(property = "jsonschema2pojo.serializable", defaultValue = "false")
    private boolean serializable = false;

    /**
     * Whether to initialize Set and List fields as empty collections, or leave
     * them as <code>null</code>.
     *
     * @since
     */
    @Parameter(property = "jsonschema2pojo.initializeCollections", defaultValue = "true")
    private boolean initializeCollections = true;

    /**
     * List of file patterns to include.
     *
     * @since 0.4.3
     */
    @Parameter
    private String[] includes;

    /**
     * List of file patterns to exclude. This only applies to the initial scan
     * of the file system and will not prevent inclusion through a "$ref" in one
     * of the schemas.
     *
     * @since 0.4.3
     */
    @Parameter
    private String[] excludes;

    /**
     * Whether to add a prefix to generated classes.
     *
     * @since 0.4.6
     */
    @Parameter(property = "jsonschema2pojo.classNamePrefix")
    private String classNamePrefix = "";

    /**
     * Whether to add a suffix to generated classes.
     *
     * @since 0.4.6
     */
    @Parameter(property = "jsonschema2pojo.classNameSuffix")
    private String classNameSuffix = "";

    /**
     * The strings (no preceeding dot) that should be considered as file name
     * extensions, and therefore ignored, when creating Java class names.
     *
     * @since 0.4.23
     */
    @Parameter(property = "jsonschema2pojo.fileExtensions", defaultValue = "")
    private String[] fileExtensions = new String[] {};

    /**
     * Whether to generate constructors or not
     *
     * @since 0.4.8
     */
    @Parameter(property = "jsonschema2pojo.includeConstructors", defaultValue = "false")
    private boolean includeConstructors = false;

    /**
     * The 'constructorsRequiredPropertiesOnly' configuration option.
     * This is a legacy configuration option used to turn on {@link #isIncludeRequiredPropertiesConstructor()}
     * and off the {@link #isIncludeAllPropertiesConstructor()} configuration options.
     * It is specifically tied to the {@link #isIncludeConstructors()} property, and will do nothing if that property is not enabled
     *
     * @since 0.4.8
     */
    @Parameter(property = "jsonschema2pojo.constructorsRequiredPropertiesOnly", defaultValue = "false")
    private boolean constructorsRequiredPropertiesOnly = false;

    /**
     * The 'includeRequiredPropertiesConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor with only the required properties as parameters.
     *
     * @since 1.0.3
     */
    @Parameter(property = "jsonschema2pojo.includeRequiredPropertiesConstructor", defaultValue = "false")
    private boolean includeRequiredPropertiesConstructor = false;

    /**
     * The 'includeAllPropertiesConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor with all listed properties as parameters.
     *
     * @since 1.0.3
     */
    @Parameter(property = "jsonschema2pojo.includeAllPropertiesConstructor", defaultValue = "true")
    private boolean includeAllPropertiesConstructor = true;

    /**
     * The 'includeCopyConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor the class itself as a parameter, with the expectation that all properties from the
     * originating class will assigned to the new class.
     *
     * @since 1.0.3
     */
    @Parameter(property = "jsonschema2pojo.includeCopyConstructor", defaultValue = "false")
    private boolean includeCopyConstructor = false;

    /**
     * Whether to allow 'additional properties' support in objects. Setting this
     * to false will disable additional properties support, regardless of the
     * input schema(s).
     *
     * @since 0.4.14
     */
    @Parameter(property = "jsonschema2pojo.includeAdditionalProperties", defaultValue = "true")
    private boolean includeAdditionalProperties = true;

    /**
     * Whether to include getters or to omit this accessor method and
     * create public fields instead
     *
     */
    @Parameter(property = "jsonschema2pojo.includeGetters", defaultValue = "true")
    private boolean includeGetters = true;

    /**
     * Whether to include setters or to omit this accessor method and
     * create public fields instead
     *
     */
    @Parameter(property = "jsonschema2pojo.includeSetters", defaultValue = "true")
    private boolean includeSetters = true;

    /**
     * The target version for generated source files, used whenever decisions are made
     * about generating source code that may be incompatible with older JVMs. Acceptable values
     * include e.g. 1.6, 1.8, 8, 9, 10, 11.
     * <p/>
     * If not set, the value of targetVersion is auto-detected. For auto-detection, the first
     * value found in the following list will be used:
     * <ol>
     *     <li>maven.compiler.source property</li>
     *     <li>maven.compiler.release property</li>
     *     <li>maven-compiler-plugin 'source' configuration option</li>
     *     <li>maven-compiler-plugin 'release' configuration option</li>
     *     <li>the current JVM version</li>
     * </ol>
     */
    @Parameter(property = "jsonschema2pojo.targetVersion")
    private String targetVersion;

    /**
     * Whether to include dynamic getters, setters, and builders or to omit
     * these methods.
     *
     * @since 0.4.17
     */
    @Parameter(property = "jsonschema2pojo.includeDynamicAccessors")
    private boolean includeDynamicAccessors = false;

    /**
     * Whether to include dynamic getters or to omit these methods.
     */
    @Parameter(property = "jsonschema2pojo.includeDynamicGetters", defaultValue = "false")
    private boolean includeDynamicGetters = false;

    /**
     * Whether to include dynamic setters or to omit these methods.
     *
     */
    @Parameter(property = "jsonschema2pojo.includeDynamicSetters", defaultValue = "false")
    private boolean includeDynamicSetters = false;

    /**
     * Whether to include dynamic builders or to omit these methods.
     *
     */
    @Parameter(property = "jsonschema2pojo.includeDynamicBuilders", defaultValue = "false")
    private boolean includeDynamicBuilders = false;

    /**
     * The project being built.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Whether the fields of type `date` are formatted during serialization with
     * a default pattern of yyyy-MM-dd.
     *
     * @since 0.4.33
     */
    @Parameter(property = "jsonschema2pojo.formatDates", defaultValue = "false")
    private boolean formatDates = false;

    /**
     * Whether the fields of type `time` are formatted during serialization with
     * a default pattern of HH:mm:ss.SSS.
     *
     * @since 0.4.36
     */
    @Parameter(property = "jsonschema2pojo.formatTimes", defaultValue = "false")
    private boolean formatTimes = false;

    /**
     * Whether the fields of type `date` are formatted during serialization with
     * a default pattern of yyyy-MM-dd'T'HH:mm:ss.SSSZ.
     *
     * @since 0.4.29
     */
    @Parameter(property = "jsonschema2pojo.formatDateTimes", defaultValue = "false")
    private boolean formatDateTimes = false;

    /**
     * A custom pattern to use when formatting date fields during serialization.
     * Requires support from your JSON binding library.
     *
     * @since 0.4.33
     */
    @Parameter(property = "jsonschema2pojo.customDatePattern")
    private String customDatePattern;

    /**
     * A custom pattern to use when formatting time fields during serialization.
     * Requires support from your JSON binding library.
     *
     * @since 0.4.36
     */
    @Parameter(property = "jsonschema2pojo.customTimePattern")
    private String customTimePattern;

    /**
     * A custom pattern to use when formatting date-time fields during
     * serialization. Requires support from your JSON binding library.
     *
     * @since 0.4.33
     */
    @Parameter(property = "jsonschema2pojo.customDatePattern")
    private String customDateTimePattern;

    /**
     * A string containing any characters that should act as path delimiters when resolving $ref fragments.
     * By default, #, / and . are used in an attempt to support JSON Pointer and JSON Path.
     *
     * @since 0.4.31
     */
    @Parameter(property = "jsonschema2pojo.refFragmentPathDelimiters", defaultValue = "#/.")
    private String refFragmentPathDelimiters = "#/.";

    private FileFilter fileFilter = new AllFileFilter();

    /**
     * The sort order to be applied when recursively processing the source
     * files. By default the OS can influence the processing order.   Supported values:
     * <ul>
     * <li><code>OS</code> (Let the OS influence the order the source files are processed.)</li>
     * <li><code>FILES_FIRST</code> (Case sensitive sort, visit the files first.  The source files are processed in a breadth
     * first sort order.)</li>
     * <li><code>SUBDIRS_FIRST</code> (Case sensitive sort, visit the sub-directories before the files.  The source files are
     * processed in a depth first sort order.)</li>
     * </ul>
     *
     * @since 0.4.34
     */
    @Parameter(property = "jsonschema2pojo.sourceSortOrder", defaultValue = "OS")
    private String sourceSortOrder = SourceSortOrder.OS.toString();

    /**
     * @since 1.0.0
     */
    @Parameter(property = "jsonschema2pojo.formatTypeMapping", defaultValue = "")
    private Map<String, String> formatTypeMapping = new HashMap<>();

    /**
     * If set to true, then the gang of four builder pattern will be used to generate builders on generated classes. Note: This property works
     * in collaboration with the {@link #isGenerateBuilders()} method. If the {@link #isGenerateBuilders()} is false,
     * then this property will not do anything.
     *
     * @since 1.0.0
     */
    @Parameter(property = "jsonschema2pojo.useInnerClassBuilders", defaultValue = "false")
    private boolean useInnerClassBuilders = false;

    /**
     * @since 1.0.2
     */
    @Parameter(property = "jsonschema2pojo.includeConstructorPropertiesAnnotation", defaultValue = "false")
    private boolean includeConstructorPropertiesAnnotation = false;

    /**
     * Whether to include a javax.annotation.Generated (Java 8 and
     * lower) or javax.annotation.processing.Generated (Java 9+) in
     * on generated types. See also: targetVersion.
     */
    @Parameter(property = "jsonschema2pojo.includeGeneratedAnnotation", defaultValue = "true")
    private boolean includeGeneratedAnnotation = true;

    /**
     * Whether to use annotations from {@code jakarta.validation} package instead of {@code javax.validation} package
     * when adding <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a> annotations to generated Java types.
     * This property works in collaboration with the {@link #isIncludeJsr303Annotations()} configuration option.
     * If the {@link #isIncludeJsr303Annotations()} returns {@code false}, then this configuration option will not affect anything.
     */
    @Parameter(property = "jsonschema2pojo.useJakartaValidation", defaultValue = "false")
    private boolean useJakartaValidation = false;

    /**
     * Executes the plugin, to read the given source and behavioural properties
     * and generate POJOs. The current implementation acts as a wrapper around
     * the command line interface.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "Private fields set by Maven.")
    @SuppressWarnings("PMD.UselessParentheses")
    public void execute() throws MojoExecutionException {

        addProjectDependenciesToClasspath();
        setTargetVersion();

        try {
            getAnnotationStyle();
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Not a valid annotation style: " + annotationStyle);
        }

        try {
            new AnnotatorFactory(this).getAnnotator(getCustomAnnotator());
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (skip) {
            return;
        }

        // verify source directories
        if (sourceDirectory != null) {
            sourceDirectory = FilenameUtils.normalize(sourceDirectory);
            // verify sourceDirectory
            try {
                URLUtil.parseURL(sourceDirectory);
            } catch (IllegalArgumentException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } else if (!isEmpty(sourcePaths)) {
            // verify individual source paths
            for (int i = 0; i < sourcePaths.length; i++) {
                sourcePaths[i] = URLUtil.isLocalUrl(sourcePaths[i]) ?
                        FilenameUtils.normalize(sourcePaths[i]) : sourcePaths[i];
                try {
                    URLUtil.parseURL(sourcePaths[i]);
                } catch (IllegalArgumentException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } else {
            throw new MojoExecutionException("One of sourceDirectory or sourcePaths must be provided");
        }

        if (filteringEnabled() || (sourceDirectory != null && isEmpty(sourcePaths))) {

            if (sourceDirectory == null) {
                throw new MojoExecutionException("Source includes and excludes require the sourceDirectory property");
            }

            if (!isEmpty(sourcePaths)) {
                throw new MojoExecutionException("Source includes and excludes are incompatible with the sourcePaths property");
            }

            fileFilter = createFileFilter();
        }

        if (addCompileSourceRoot) {
            project.addCompileSourceRoot(outputDirectory.getPath());
        }

        RuleLogger logger = new MojoRuleLogger(getLog());

        try {
            Jsonschema2Pojo.generate(this, logger);
        } catch (IOException e) {
            throw new MojoExecutionException("Error generating classes from JSON Schema file(s) " + sourceDirectory, e);
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

    @Override
    public boolean isGenerateBuilders() {
        return generateBuilders;
    }

    @Override
    public boolean isIncludeTypeInfo()
    {
        return includeTypeInfo;
    }

    @Override
    public boolean isIncludeConstructorPropertiesAnnotation()
    {
        return includeConstructorPropertiesAnnotation;
    }

    @Override
    public File getTargetDirectory() {
        return outputDirectory;
    }

    @Override
    public Iterator<URL> getSource() {
        if (null != sourceDirectory) {
            return Collections.singleton(URLUtil.parseURL(sourceDirectory)).iterator();
        }
        List<URL> sourceURLs = new ArrayList<>();
        for (String source : sourcePaths) {
            sourceURLs.add(URLUtil.parseURL(source));
        }
        return sourceURLs.iterator();
    }

    @Override
    public boolean isUsePrimitives() {
        return usePrimitives;
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    @Override
    public char[] getPropertyWordDelimiters() {
        return propertyWordDelimiters.toCharArray();
    }

    @Override
    public boolean isUseLongIntegers() {
        return useLongIntegers;
    }

    @Override
    public boolean isUseDoubleNumbers() {
        return useDoubleNumbers;
    }

    @Override
    public boolean isIncludeHashcodeAndEquals() {
        return includeHashcodeAndEquals;
    }

    @Override
    public boolean isIncludeToString() {
        return includeToString;
    }

    @Override
    public String[] getToStringExcludes() {
        return toStringExcludes;
    }

    @Override
    public AnnotationStyle getAnnotationStyle() {
        return AnnotationStyle.valueOf(annotationStyle.toUpperCase());
    }

    @Override
    public boolean isUseTitleAsClassname() {
        return useTitleAsClassname;
    }

    @Override
    public InclusionLevel getInclusionLevel() {
        return InclusionLevel.valueOf(inclusionLevel.toUpperCase());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Annotator> getCustomAnnotator() {
        if (isNotBlank(customAnnotator)) {
            try {
                return (Class<? extends Annotator>) Class.forName(customAnnotator);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return NoopAnnotator.class;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends RuleFactory> getCustomRuleFactory() {
        if (isNotBlank(customRuleFactory)) {
            try {
                return (Class<? extends RuleFactory>) Class.forName(customRuleFactory);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return RuleFactory.class;
        }
    }

    @Override
    public boolean isIncludeJsr303Annotations() {
        return includeJsr303Annotations;
    }

    @Override
    public boolean isIncludeJsr305Annotations() {
        return includeJsr305Annotations;
    }

    @Override
    public boolean isUseOptionalForGetters() {
        return useOptionalForGetters;
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.valueOf(sourceType.toUpperCase());
    }

    @Override
    public boolean isRemoveOldOutput() {
        return removeOldOutput;
    }

    @Override
    public String getOutputEncoding() {
        return outputEncoding;
    }

    @Override
    public boolean isUseJodaDates() {
        return useJodaDates;
    }

    @Override
    public boolean isUseJodaLocalDates() {
        return useJodaLocalDates;
    }

    @Override
    public boolean isUseJodaLocalTimes() {
        return useJodaLocalTimes;
    }

    @Override
    public boolean isParcelable() {
        return parcelable;
    }

    @Override
    public boolean isSerializable() {
        return serializable;
    }

    @Override
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    @Override
    public boolean isInitializeCollections() {
        return initializeCollections;
    }

    boolean filteringEnabled() {
        return !((includes == null || includes.length == 0) && (excludes == null || excludes.length == 0));
    }

    FileFilter createFileFilter() throws MojoExecutionException {
        try {
            URL urlSource = URLUtil.parseURL(sourceDirectory);
            return new MatchPatternsFileFilter.Builder().addIncludes(includes).addExcludes(excludes).addDefaultExcludes().withSourceDirectory(URLUtil.getFileFromURL(urlSource).getCanonicalPath()).withCaseSensitive(false).build();
        } catch (IOException e) {
            throw new MojoExecutionException("could not create file filter", e);
        }
    }

    @Override
    public String getClassNamePrefix() {
        return classNamePrefix;
    }

    @Override
    public String getClassNameSuffix() {
        return classNameSuffix;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtensions;
    }

    @Override
    public boolean isIncludeConstructors() {
        return includeConstructors;
    }

    @Override
    public boolean isConstructorsRequiredPropertiesOnly() {
        return constructorsRequiredPropertiesOnly;
    }

    @Override
    public boolean isIncludeRequiredPropertiesConstructor() {
        return includeRequiredPropertiesConstructor;
    }

    @Override
    public boolean isIncludeAllPropertiesConstructor() {
        return includeAllPropertiesConstructor;
    }

    @Override
    public boolean isIncludeCopyConstructor() {
        return includeCopyConstructor;
    }

    @Override
    public boolean isIncludeAdditionalProperties() {
        return includeAdditionalProperties;
    }

    @Override
    public boolean isIncludeGetters() { return includeGetters; }

    @Override
    public boolean isIncludeSetters() { return includeSetters; }

    @SuppressWarnings("unchecked")
    private void setTargetVersion() {
        if (isNotBlank(this.targetVersion)) {
            return;
        }

        if (project.getProperties() != null && project.getProperties().containsKey("maven.compiler.source")) {
            this.targetVersion = project.getProperties().get("maven.compiler.source").toString();
            getLog().debug("Using maven.compiler.source to set targetVersion for generated sources (" + this.targetVersion + ")");
            return;
        }

        if (project.getProperties() != null && project.getProperties().containsKey("maven.compiler.release")) {
            this.targetVersion = project.getProperties().get("maven.compiler.release").toString();
            getLog().debug("Using maven.compiler.release to set targetVersion for generated sources (" + this.targetVersion + ")");
            return;
        }

        for (Plugin p : (List<Plugin>) project.getBuildPlugins()) {
            if (p.getKey().equals("org.apache.maven.plugins:maven-compiler-plugin") && p.getConfiguration() instanceof Xpp3Dom) {
                final Xpp3Dom compilerSourceConfig = ((Xpp3Dom) p.getConfiguration()).getChild("source");
                if (compilerSourceConfig != null) {
                    this.targetVersion = compilerSourceConfig.getValue();
                    getLog().debug("Using maven-compiler-plugin 'source' to set targetVersion for generated sources (" + this.targetVersion + ")");
                    return;
                }

                final Xpp3Dom compilerReleaseConfig = ((Xpp3Dom) p.getConfiguration()).getChild("release");
                if (compilerReleaseConfig != null) {
                    this.targetVersion = compilerReleaseConfig.getValue();
                    getLog().debug("Using maven-compiler-plugin 'release' to set targetVersion for generated sources (" + this.targetVersion + ")");
                    return;
                }
            }
        }

        this.targetVersion = JavaVersion.parse(System.getProperty("java.version"));
        getLog().debug("Using JVM to set targetVersion for generated sources (" + this.targetVersion + ")");
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    @Override
    public boolean isIncludeDynamicAccessors() {
        return includeDynamicAccessors;
    }

    @Override
    public boolean isIncludeDynamicGetters() {
        return includeDynamicGetters;
    }

    @Override
    public boolean isIncludeDynamicSetters() {
        return includeDynamicSetters;
    }

    @Override
    public boolean isIncludeDynamicBuilders() {
        return includeDynamicBuilders;
    }

    @Override
    public String getDateTimeType() {
        return dateTimeType;
    }

    @Override
    public String getDateType() {
        return dateType;
    }

    @Override
    public String getTimeType() {
        return timeType;
    }

    @Override
    public boolean isUseBigIntegers() {
        return useBigIntegers;
    }

    @Override
    public boolean isUseBigDecimals() {
        return useBigDecimals;
    }

    @Override
    public boolean isFormatDateTimes() {
        return formatDateTimes;
    }

    @Override
    public boolean isFormatDates() {
        return formatDates;
    }

    @Override
    public boolean isFormatTimes() {
        return formatTimes;
    }

    @Override
    public String getCustomDatePattern() {
        return customDatePattern;
    }

    @Override
    public String getCustomTimePattern() {
        return customTimePattern;
    }

    @Override
    public String getCustomDateTimePattern() {
        return customDateTimePattern;
    }

    @Override
    public String getRefFragmentPathDelimiters() {
        return refFragmentPathDelimiters;
    }

    @Override
    public SourceSortOrder getSourceSortOrder() {
        return SourceSortOrder.valueOf(sourceSortOrder.toUpperCase());
    }

    @Override
    public Map<String, String> getFormatTypeMapping() {
        return formatTypeMapping;
    }

    @Override
    public boolean isUseInnerClassBuilders() {
        return useInnerClassBuilders;
    }

    @Override
    public boolean isIncludeGeneratedAnnotation() {
        return includeGeneratedAnnotation;
    }

    @Override
    public boolean isUseJakartaValidation() {
        return useJakartaValidation;
    }
}
