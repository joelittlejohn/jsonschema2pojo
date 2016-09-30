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

package org.jsonschema2pojo.maven;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.AllFileFilter;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.AnnotatorFactory;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.URLUtil;

/**
 * When invoked, this goal reads one or more
 * <a href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @threadSafe
 * @see <a href=
 *      "http://maven.apache.org/developers/mojo-api-specification.html">Mojo
 *      API Specification</a>
 */
public class Jsonschema2PojoMojo extends AbstractMojo implements GenerationConfig {

    /**
     * Target directory for generated Java source files.
     *
     * @parameter expression="${jsonschema2pojo.outputDirectory}"
     *            default-value="${project.build.directory}/java-gen"
     * @since 0.1.0
     */
    private File outputDirectory;

    /**
     * Location of the JSON Schema file(s). Note: this may refer to a single
     * file or a directory of files.
     *
     * @parameter expression="${jsonschema2pojo.sourceDirectory}"
     * @since 0.1.0
     */
    private String sourceDirectory;

    /**
     * An array of locations of the JSON Schema file(s). Note: each item may
     * refer to a single file or a directory of files.
     *
     * @parameter expression="${jsonschema2pojo.sourcePaths}"
     * @since 0.3.1
     */
    private String[] sourcePaths;

    /**
     * Package name used for generated Java classes (for types where a fully
     * qualified name has not been supplied in the schema using the 'javaType'
     * property).
     *
     * @parameter expression="${jsonschema2pojo.targetPackage}"
     * @since 0.1.0
     */
    private String targetPackage = "";

    /**
     * Whether to use contextual sub-packages or not.
     *
     * @parameter expression="${jsonschema2pojo.useContextualSubPackages}"
     * @since 0.4.27
     */
    private boolean useContextualSubPackages = false;

    /**
     * Whether to generate builder-style methods of the form
     * <code>withXxx(value)</code> (that return <code>this</code>), alongside
     * the standard, void-return setters.
     *
     * @parameter expression="${jsonschema2pojo.generateBuilders}"
     *            default-value="false"
     * @since 0.1.2
     */
    private boolean generateBuilders = false;

    /**
     * Whether to use primitives (<code>long</code>, <code>double</code>,
     * <code>boolean</code>) instead of wrapper types where possible when
     * generating bean properties (has the side-effect of making those
     * properties non-null).
     *
     * @parameter expression="${jsonschema2pojo.usePrimitives}"
     *            default-value="false"
     * @since 0.2.0
     */
    private boolean usePrimitives = false;

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
     * Skip plugin execution (don't read/validate any schema files, don't
     * generate any java types).
     *
     * @parameter expression="${jsonschema2pojo.skip}" default-value="false"
     * @since 0.2.1
     */
    private boolean skip = false;

    /**
     * The characters that should be considered as word delimiters when creating
     * Java Bean property names from JSON property names. If blank or not set,
     * JSON properties will be considered to contain a single word when creating
     * Java Bean property names.
     *
     * @parameter expression="${jsonschema2pojo.propertyWordDelimiters}"
     *            default-value="- _"
     * @since 0.2.2
     */
    private String propertyWordDelimiters = "- _";

    /**
     * Whether to use the java type <code>long</code> (or <code>Long</code>)
     * instead of <code>int</code> (or <code>Integer</code>) when representing
     * the JSON Schema type 'integer'.
     *
     * @parameter expression="${jsonschema2pojo.useLongIntegers}"
     *            default-value="false"
     * @since 0.2.2
     */
    private boolean useLongIntegers = false;

    /**
     * Whether to use the java type {@link java.math.BigInteger} instead of
     * <code>int</code> (or {@link java.lang.Integer}) when representing the
     * JSON Schema type 'integer'. Note that this configuration overrides
     * {@link #isUseLongIntegers()}.
     *
     * @parameter expression="${jsonschema2pojo.useBigIntegers}"
     *            default-value="false"
     * @since 0.4.25
     */
    private boolean useBigIntegers = false;

    /**
     * Whether to use the java type <code>double</code> (or <code>Double</code>)
     * instead of <code>float</code> (or <code>Float</code>) when representing
     * the JSON Schema type 'number'.
     *
     * @parameter expression="${jsonschema2pojo.useDoubleNumbers}"
     *            default-value="true"
     * @since 0.4.0
     */
    private boolean useDoubleNumbers = true;

    /**
     * Whether to use the java type {@link java.math.BigDecimal} instead of
     * <code>float</code> (or {@link java.lang.Float}) when representing the
     * JSON Schema type 'number'. Note that this configuration overrides
     * {@link #isUseDoubleNumbers()}.
     *
     * @parameter expression="${jsonschema2pojo.useBigDecimals}"
     *            default-value="false"
     * @since 0.4.22
     */
    private boolean useBigDecimals = false;

    /**
     * Whether to include <code>hashCode</code> and <code>equals</code> methods
     * in generated Java types.
     *
     * @parameter expression="${jsonschema2pojo.includeHashcodeAndEquals}"
     *            default-value="true"
     * @since 0.3.1
     */
    private boolean includeHashcodeAndEquals = true;

    /**
     * Whether to include a <code>toString</code> method in generated Java
     * types.
     *
     * @parameter expression="${jsonschema2pojo.includeToString}"
     *            default-value="true"
     * @since 0.3.1
     */
    private boolean includeToString = true;

    /**
     * The style of annotations to use in the generated Java types.
     * <p>
     * Supported values:
     * <ul>
     * <li><code>jackson2</code> (apply annotations from the
     * <a href="https://github.com/FasterXML/jackson-annotations">Jackson
     * 2.x</a> library)</li>
     * <li><code>jackson1</code> (apply annotations from the
     * <a href="http://jackson.codehaus.org/">Jackson 1.x</a> library)</li>
     * <li><code>gson</code> (apply annotations from the
     * <a href="https://code.google.com/p/google-gson/">gson</a> library)</li>
     * <li><code>moshi1</code> (apply annotations from the
     * <a href="https://github.com/square/moshi">moshi 1.x</a> library)</li>
     * <li><code>none</code> (apply no annotations at all)</li>
     * </ul>
     *
     * @parameter expression="${jsonschema2pojo.annotationStyle}"
     *            default-value="jackson2"
     * @since 0.3.1
     */
    private String annotationStyle = "jackson2";

    /**
     * A fully qualified class name, referring to a custom annotator class that
     * implements <code>org.jsonschema2pojo.Annotator</code> and will be used in
     * addition to the one chosen by <code>annotationStyle</code>.
     * <p>
     * If you want to use the custom annotator alone, set
     * <code>annotationStyle</code> to <code>none</code>.
     *
     * @parameter expression="${jsonschema2pojo.customAnnotator}"
     *            default-value="org.jsonschema2pojo.NoopAnnotator"
     * @since 0.3.6
     */
    private String customAnnotator = NoopAnnotator.class.getName();

    /**
     * A fully qualified class name, referring to an class that extends
     * <code>org.jsonschema2pojo.rules.RuleFactory</code> and will be used to
     * create instances of Rules used for code generation.
     *
     * @parameter expression="${jsonschema2pojo.customRuleFactory}"
     *            default-value="org.jsonschema2pojo.rules.RuleFactory"
     * @since 0.4.5
     */
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
     * @parameter expression="${jsonschema2pojo.includeJsr303Annotations}"
     *            default-value="false"
     * @since 0.3.2
     */
    private boolean includeJsr303Annotations = false;

    /**
     * Whether to include
     * <a href="http://jcp.org/en/jsr/detail?id=305">JSR-305</a> annotations
     * (for schema rules like Nullable, NonNull, etc) in generated Java types.
     * @since 0.4.8
     */
    private boolean includeJsr305Annotations = false;

    /**
     * The type of input documents that will be read
     * <p>
     * Supported values:
     * <ul>
     * <li><code>jsonschema</code> (schema documents, containing formal rules
     * that describe the structure of json data)</li>
     * <li><code>json</code> (documents that represent an example of the kind of
     * json data that the generated Java types will be mapped to)</li>
     * </ul>
     *
     * @parameter expression="${jsonschema2pojo.sourceType}"
     *            default-value="jsonschema"
     * @since 0.3.3
     */
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
     * @parameter expression="${jsonschema2pojo.removeOldOutput}"
     *            default-value="false"
     * @since 0.3.7
     */
    private boolean removeOldOutput = false;

    /**
     * The character encoding that should be used when writing the generated
     * Java source files.
     *
     * @parameter expression="${jsonschema2pojo.outputEncoding}" default-value="UTF-8"
     * @since 0.4.0
     */
    private String outputEncoding = "UTF-8";

    /**
     * Whether to use {@link org.joda.time.DateTime} instead of
     * {@link java.util.Date} when adding date type fields to generated Java
     * types.
     *
     * @parameter expression="${jsonschema2pojo.useJodaDates}" default-value="false"
     * @since 0.4.0
     */
    private boolean useJodaDates = false;

    /**
     * Whether to use {@link org.joda.time.LocalDate} instead of string when
     * adding string type fields of format date (not date-time) to generated
     * Java types.
     *
     * @parameter expression="${jsonschema2pojo.useJodaLocalDates}"
     *            default-value="false"
     * @since 0.4.9
     */
    private boolean useJodaLocalDates = false;

    /**
     * Whether to use {@link org.joda.time.LocalTime} instead of string when
     * adding string type fields of format time (not date-time) to generated
     * Java types.
     *
     * @parameter expression="${jsonschema2pojo.useJodaLocalTimes}"
     *            default-value="false"
     * @since 0.4.9
     */
    private boolean useJodaLocalTimes = false;

    /**
     * What type to use instead of string when adding string type fields of
     * format date-time to generated Java types.
     *
     * @parameter expression="${jsonschema2pojo.dateTimeType}"
     * @since 0.4.22
     */
    private String dateTimeType = null;

    /**
     * What type to use instead of string when adding string type fields of
     * format time (not date-time) to generated Java types.
     *
     * @parameter expression="${jsonschema2pojo.timeType}"
     * @since 0.4.22
     */
    private String timeType = null;

    /**
     * What type to use instead of string when adding string type fields of
     * format date (not date-time) to generated Java types.
     *
     * @parameter expression="${jsonschema2pojo.dateType}"
     * @since 0.4.22
     */
    private String dateType = null;

    /**
     * Whether to use commons-lang 3.x imports instead of commons-lang 2.x
     * imports when adding equals, hashCode and toString methods.
     *
     * @parameter expression="${jsonschema2pojo.useCommonsLang3}"
     *            default-value="false"
     * @since 0.4.1
     */
    private boolean useCommonsLang3 = false;

    /**
     * **EXPERIMENTAL** Whether to make the generated types 'parcelable' (for
     * Android development).
     *
     * @parameter expression="${jsonschema2pojo.parcelable}" default-value="false"
     * @since 0.4.11
     */
    private boolean parcelable = false;

    /**
     * Whether to make the generated types 'serializable'.
     *
     * @parameter expression="${jsonschema2pojo.serializable}" default="false"
     * @since 0.4.23
     */
    private boolean serializable = false;

    /**
     * Whether to initialize Set and List fields as empty collections, or leave
     * them as <code>null</code>.
     *
     * @parameter expression="${jsonschema2pojo.initializeCollections}"
     *            default-value="true"
     * @since
     */
    private boolean initializeCollections = true;

    /**
     * List of file patterns to include.
     *
     * @parameter
     * @since 0.4.3
     */
    private String[] includes;

    /**
     * List of file patterns to exclude.  This only applies to the initial scan of
     * the file system and will not prevent inclusion through a "$ref" in one of the
     * schemas.
     *
     * @parameter
     * @since 0.4.3
     */
    private String[] excludes;

    /**
     * Whether to add a prefix to generated classes.
     *
     * @parameter expression="${jsonschema2pojo.classNamePrefix}"
     * @since 0.4.6
     */
    private String classNamePrefix = "";

    /**
     * Whether to add a suffix to generated classes.
     *
     * @parameter expression="${jsonschema2pojo.classNameSuffix}"
     * @since 0.4.6
     */
    private String classNameSuffix = "";

    /**
     * Whether to use contextual class names or not.
     *
     * @parameter expression="${jsonschema2pojo.useContextualClassNames}"
     * @since 0.4.27
     */
    private boolean useContextualClassNames = false;

    /**
     * Defines the delimiter for the contextual part of class names.
     *
     * @parameter expression="${jsonschema2pojo.contextualClassNameDelimiter}"
     * @since 0.4.27
     */
    private String contextualClassNameDelimiter = "";

    /**
     * The file extenations that should be considered as file name extensions,
     * and therefore ignored, when creating Java class names.
     *
     * @parameter expression="${jsonschema2pojo.fileExtensions}"
     *            default-value=""
     * @since 0.4.23
     */
    private String[] fileExtensions = new String[] {};

    /**
     * Whether to generate constructors or not
     *
     * @parameter expression="${jsonschema2pojo.includeConstructors}"
     *            default-value="false"
     * @since 0.4.8
     */
    private boolean includeConstructors = false;

    /**
     * Whether generated constructors should have parameters for all properties,
     * or only required ones.
     *
     * @parameter expression=
     *            "${jsonschema2pojo.constructorsRequiredPropertiesOnly}"
     *            default-value="false"
     * @since 0.4.8
     */
    private boolean constructorsRequiredPropertiesOnly = false;

    /**
     * Whether to allow 'additional properties' support in objects. Setting this
     * to false will disable additional properties support, regardless of the
     * input schema(s).
     *
     * @parameter expression="${jsonschema2pojo.includeAdditionalProperties}"
     *            default-value="true"
     * @since 0.4.14
     */
    private boolean includeAdditionalProperties = true;

    /**
     * Whether to include getters/setters or to omit these accessor methods and
     * create public fields instead.
     *
     * @parameter expression="${jsonschema2pojo.includeAccessors}"
     *            default-value="true"
     * @since 0.4.15
     */
    private boolean includeAccessors = true;

    /**
     * The target version for generated source files.
     *
     * @parameter expression="${maven.compiler.target}"
     *            default-value="1.6"
     * @since 0.4.17
     */
    private String targetVersion = "1.6";

    /**
     * Whether to include dynamic getters, setters, and builders or to omit these methods.
     *
     * @parameter expression="${jsonschema2pojo.includeDynamicAccessors}"
     *            default-value="false"
     * @since 0.4.17
     */
    private boolean includeDynamicAccessors = false;

    /**
     * The project being built.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private FileFilter fileFilter = new AllFileFilter();

    /**
     * Executes the plugin, to read the given source and behavioural properties
     * and generate POJOs. The current implementation acts as a wrapper around
     * the command line interface.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "Private fields set by Maven.")
    public void execute() throws MojoExecutionException {

        addProjectDependenciesToClasspath();

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
            // verify sourceDirectory
            try {
                URLUtil.parseURL(sourceDirectory);
            } catch (IllegalArgumentException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } else if (!isEmpty(sourcePaths)) {
            // verify individual source paths
            for (String source : sourcePaths) {
                try {
                    URLUtil.parseURL(source);
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

        try {
            Jsonschema2Pojo.generate(this);
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
    public File getTargetDirectory() {
        return outputDirectory;
    }

    @Override
    public Iterator<URL> getSource() {
        if (null != sourceDirectory) {
            return Collections.singleton(URLUtil.parseURL(sourceDirectory)).iterator();
        }
        List<URL> sourceURLs = new ArrayList<URL>();
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
    public boolean isUseContextualSubPackages() {
        return useContextualSubPackages;
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
    public AnnotationStyle getAnnotationStyle() {
        return AnnotationStyle.valueOf(annotationStyle.toUpperCase());
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
    public boolean isUseCommonsLang3() {
        return useCommonsLang3;
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
    public boolean isUseContextualClassNames() {
        return useContextualClassNames;
    }

    @Override
    public String getContextualClassNameDelimiter() {
        return contextualClassNameDelimiter;
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
    public boolean isIncludeAdditionalProperties() {
        return includeAdditionalProperties;
    }

    @Override
    public boolean isIncludeAccessors() {
        return includeAccessors;
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

}
