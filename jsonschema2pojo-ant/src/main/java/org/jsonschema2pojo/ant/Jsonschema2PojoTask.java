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

package org.jsonschema2pojo.ant;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.jsonschema2pojo.AllFileFilter;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.InclusionLevel;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.SourceSortOrder;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.URLProtocol;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.URLUtil;

/**
 * When invoked, this task reads one or more
 * <a href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 * <p>
 * See <a href=
 * "http://jsonschema2pojo.googlecode.com">jsonschema2pojo.googlecode.com</a>.
 *
 * @see <a href="http://ant.apache.org/manual/develop.html">Writing Your Own
 *      Task</a>
 */
public class Jsonschema2PojoTask extends Task implements GenerationConfig {

    private boolean generateBuilders;

    private boolean includeTypeInfo = false;

    private boolean useInnerClassBuilders = false;

    private boolean includeConstructorPropertiesAnnotation = false;

    private boolean includeConstructors = false;

    private boolean usePrimitives;

    private String source;

    private File targetDirectory;

    private String targetPackage;

    private boolean skip;

    private char[] propertyWordDelimiters = new char[] { '-', ' ', '_' };

    private boolean useLongIntegers;

    private boolean useBigIntegers = false;

    private boolean useDoubleNumbers = true;

    private boolean useBigDecimals = false;

    private boolean includeHashcodeAndEquals = true;

    private boolean includeToString = true;

    private String[] toStringExcludes = new String[] {};

    private AnnotationStyle annotationStyle = AnnotationStyle.JACKSON;

    private boolean useTitleAsClassname = false;

    private InclusionLevel inclusionLevel = InclusionLevel.NON_NULL;

    private Class<? extends Annotator> customAnnotator = NoopAnnotator.class;

    private Class<? extends RuleFactory> customRuleFactory = RuleFactory.class;

    private boolean includeJsr303Annotations = false;

    private boolean includeJsr305Annotations = false;

    private boolean useOptionalForGetters;

    private SourceType sourceType = SourceType.JSONSCHEMA;

    private Path classpath;

    private boolean removeOldOutput = false;

    private String outputEncoding = "UTF-8";

    private boolean useJodaDates = false;

    private boolean useJodaLocalDates = false;

    private boolean useJodaLocalTimes = false;

    private boolean parcelable = false;

    private boolean serializable = false;

    private boolean initializeCollections = true;

    private String classNamePrefix = "";

    private String classNameSuffix = "";

    private String[] fileExtensions = new String[] {};

    private boolean constructorsRequiredPropertiesOnly = false;

    private boolean includeRequiredPropertiesConstructor = false;

    private boolean includeAllPropertiesConstructor = true;

    private boolean includeCopyConstructor = false;

    private boolean includeAdditionalProperties = true;

    private boolean includeGetters = true;

    private boolean includeSetters = true;

    private String targetVersion = "1.6";

    private boolean includeDynamicAccessors = false;

    private boolean includeDynamicGetters = false;

    private boolean includeDynamicSetters = false;

    private boolean includeDynamicBuilders = false;

    private String dateTimeType;

    private String timeType;

    private String dateType;

    private boolean formatDateTimes = false;

    private boolean formatDates = false;

    private boolean formatTimes = false;

    private String customDatePattern;

    private String customTimePattern;

    private String customDateTimePattern;

    private String refFragmentPathDelimiters = "#/.";

    private SourceSortOrder sourceSortOrder = SourceSortOrder.OS;

    private Map<String, String> formatTypeMapping = new HashMap<>();
    
    private boolean includeGeneratedAnnotation = true;

    private boolean useJakartaValidation = false;
    /**
     * Execute this task (it's expected that all relevant setters will have been
     * called by Ant to provide task configuration <em>before</em> this method
     * is called).
     *
     * @throws BuildException
     *             if this task cannot be completed due to some error reading
     *             schemas, generating types or writing output .java files.
     */
    @Override
    public void execute() throws BuildException {

        if (skip) {
            return;
        }

        if (source == null) {
            log("source attribute is required but was not set");
            return;
        }

        // attempt to parse the url
        URL sourceURL;
        try {
            sourceURL = URLUtil.parseURL(source);
        } catch (IllegalArgumentException e) {
            log(String.format("Invalid schema source provided: %s", source));
            return;
        }

        // if url is a file, ensure it exists
        if (URLUtil.parseProtocol(sourceURL.toString()) == URLProtocol.FILE) {
            File sourceFile = new File(sourceURL.getFile());
            if (!sourceFile.exists()) {
                log(sourceFile.getAbsolutePath() + " cannot be found");
                return;
            }
        }

        if (targetDirectory == null) {
            log("targetDirectory attribute is required but was not set");
            return;
        }

        ClassLoader extendedClassloader = buildExtendedClassloader();
        Thread.currentThread().setContextClassLoader(extendedClassloader);

        RuleLogger ruleLogger = new AntRuleLogger(this);

        try {
            Jsonschema2Pojo.generate(this, ruleLogger);
        } catch (IOException e) {
            throw new BuildException("Error generating classes from JSON Schema file(s) " + source, e);
        }
    }

    /**
     * Build a classloader using the additional elements specified in
     * <code>classpath</code> and <code>classpathRef</code>.
     *
     * @return a new classloader that includes the extra path elements found in
     *         the <code>classpath</code> and <code>classpathRef</code> config
     *         values
     */
    private ClassLoader buildExtendedClassloader() {
        final List<URL> classpathUrls = new ArrayList<>();
        for (String pathElement : getClasspath().list()) {
            try {
                classpathUrls.add(new File(pathElement).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new BuildException("Unable to use classpath entry as it could not be understood as a valid URL: " + pathElement, e);
            }
        }

        final ClassLoader parentClassloader = Thread.currentThread().getContextClassLoader();

        return new URLClassLoader(classpathUrls.toArray(new URL[0]), parentClassloader);
    }

    /**
     * Sets the 'includeConstructors' property of this class
     *
     * @param includeConstructors
     *            Whether to generate constructors or not.
     */
    public void setIncludeConstructors(boolean includeConstructors) {
        this.includeConstructors = includeConstructors;
    }

    /**
     * Sets the 'constructorsRequiredPropertiesOnly' property of this class.
     * This is a legacy configuration option used to turn on the {@link #isIncludeRequiredPropertiesConstructor()}
     * and off the {@link #isIncludeAllPropertiesConstructor()} configuration options.
     * It is specifically tied to the {@link #isIncludeConstructors()} property, and will do nothing if that property is not enabled
     *
     * @param constructorsRequiredPropertiesOnly controls whether the resulting class will have only the constructor with required or all properties
     */
    public void setConstructorsRequiredPropertiesOnly(boolean constructorsRequiredPropertiesOnly) {
        this.constructorsRequiredPropertiesOnly = constructorsRequiredPropertiesOnly;
    }

    /**
     * Sets the 'includeRequiredPropertiesConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor with only the required properties as parameters.
     *
     * @param includeRequiredPropertiesConstructor controls whether the resulting class will include a constructor which takes in only
     * required properties
     */
    public void setIncludeRequiredPropertiesConstructor(boolean includeRequiredPropertiesConstructor) {
        this.includeRequiredPropertiesConstructor = includeRequiredPropertiesConstructor;
    }

    /**
     * Sets the 'includeAllPropertiesConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor with all listed properties as parameters.
     *
     * @param includeAllPropertiesConstructor controls whether the resulting class will include a constructor which takes in all available
     * properties of the class
     */
    public void setIncludeAllPropertiesConstructor(boolean includeAllPropertiesConstructor) {
        this.includeAllPropertiesConstructor = includeAllPropertiesConstructor;
    }

    /**
     * Sets the 'includeCopyConstructor' configuration option. This property works in collaboration with the {@link
     * #isIncludeConstructors()} configuration option and is incompatible with {@link #isConstructorsRequiredPropertiesOnly()}, and will have no effect
     * if {@link #isIncludeConstructors()} is not set to true. If {@link #isIncludeConstructors()} is set to true then this configuration determines
     * whether the resulting object should include a constructor the class itself as a parameter, with the expectation that all properties from the
     * originating class will assigned to the new class.
     *
     * @param includeCopyConstructor controls whether the resulting class will include a constructor which takes in an instance of the class
     * to be shallow copied into the new instance
     */
    public void setIncludeCopyConstructor(boolean includeCopyConstructor) {
        this.includeCopyConstructor = includeCopyConstructor;
    }

    /**
     * Sets the 'generateBuilders' property of this class.
     *
     * @param generateBuilders
     *            Whether to generate builder-style methods of the form
     *            <code>withXxx(value)</code> (that return <code>this</code>),
     *            alongside the standard, void-return setters.
     *            <p>
     *            Default: <code>false</code>.
     */
    public void setGenerateBuilders(boolean generateBuilders) {
        this.generateBuilders = generateBuilders;
    }

    /**
     * Sets the 'includeTypeInfo' property of this class.
     *
     * @param includeTypeInfo
     *            Whether to use include json type info information to class. Often required to support polymorphic type deserialization.
     *            <p>
     *            By default the type information is stored in the @class property, this can be overridden in the deserializationClassProperty of the schema.
     *            <p>
     *            Default: <code>false</code>.
     *
     * @see <a href="https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization">Jackson Polymorphic Deserialization</a>
     */
    public void setIncludeTypeInfo(boolean includeTypeInfo) {
        this.includeTypeInfo = includeTypeInfo;
    }

    /**
     * Sets the 'usePrimitives' property of this class.
     *
     * @param usePrimitives
     *            Whether to use primitives (<code>long</code>,
     *            <code>double</code> , <code>boolean</code>) instead of wrapper
     *            types where possible when generating bean properties (has the
     *            side-effect of making those properties non-null).
     *            <p>
     *            Default: <code>false</code>.
     */
    public void setUsePrimitives(boolean usePrimitives) {
        this.usePrimitives = usePrimitives;
    }

    /**
     * Sets the 'useLongIntegers' property of this class
     *
     * @param useLongIntegers
     *            Whether to use the java type <code>long</code> (or
     *            {@link java.lang.Long}) instead of <code>int</code> (or
     *            {@link java.lang.Integer}) when representing the JSON Schema
     *            type 'integer'.
     */
    public void setUseLongIntegers(boolean useLongIntegers) {
        this.useLongIntegers = useLongIntegers;
    }

    /**
     * Sets the 'useBigIntegers' property of this class.
     *
     * @param useBigIntegers
     *            Whether to use the java type {@link java.math.BigInteger}
     *            instead of <code>int</code> (or {@link java.lang.Integer})
     *            when representing the JSON Schema type 'integer'. Note that
     *            this configuration overrides {@link #isUseLongIntegers()}.
     */
    public void setUseBigIntegers(boolean useBigIntegers) {
        this.useBigIntegers = useBigIntegers;
    }

    /**
     * Sets the 'useDoubleNumbers' property of this class
     *
     * @param useDoubleNumbers
     *            Whether to use the java type <code>double</code> (or
     *            {@link java.lang.Double}) instead of <code>float</code> (or
     *            {@link java.lang.Float}) when representing the JSON Schema
     *            type 'number'.
     */
    public void setUseDoubleNumbers(boolean useDoubleNumbers) {
        this.useDoubleNumbers = useDoubleNumbers;
    }

    /**
     * Sets the 'useBigDecimals' property of this class
     *
     * @param useBigDecimals
     *            Whether to use the java type {@link java.math.BigDecimal}
     *            instead of <code>float</code> (or {@link java.lang.Float})
     *            when representing the JSON Schema type 'number'. Note that
     *            this configuration overrides {@link #isUseDoubleNumbers()}.
     */
    public void setUseBigDecimals(boolean useBigDecimals) {
        this.useBigDecimals = useBigDecimals;
    }

    /**
     * Sets schema file (or directory containing schema files) that should be
     * used for input.
     *
     * @param source
     *            Location of the JSON Schema file(s). Note: this may refer to a
     *            single file or a directory of files.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Sets the target (output) directory for generated source files.
     *
     * @param targetDirectory
     *            Target directory for generated Java source files.
     */
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    /**
     * Sets the target package for generated types.
     *
     * @param targetPackage
     *            Package name used for generated Java classes (for types where
     *            a fully qualified name has not been supplied in the schema
     *            using the 'javaType' property).
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Sets the 'skip' property of this task.
     *
     * @param skip
     *            whether to skip execution of this task
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * @param propertyWordDelimiters
     *            a string containing all of the characters that should be
     *            considered as word delimiters when creating Java Bean property
     *            names from JSON property names. If blank or not set, JSON
     *            properties will be considered to contain a single word when
     *            creating Java Bean property names.
     */
    public void setPropertyWordDelimiters(String propertyWordDelimiters) {
        this.propertyWordDelimiters = defaultString(propertyWordDelimiters).toCharArray();
    }

    /**
     * Sets the 'includeHashcodeAndEquals' property of this class
     *
     * @param includeHashcodeAndEquals
     *            Whether to include <code>hashCode</code> and
     *            <code>equals</code> methods in generated Java types.
     */
    public void setIncludeHashcodeAndEquals(boolean includeHashcodeAndEquals) {
        this.includeHashcodeAndEquals = includeHashcodeAndEquals;
    }

    /**
     * Sets the 'includeToString' property of this class
     *
     * @param includeToString
     *            Whether to include a <code>toString</code> method in generated
     *            Java types.
     */
    public void setIncludeToString(boolean includeToString) {
        this.includeToString = includeToString;
    }

    /**
     * Sets the 'annotationStyle' property of this class
     *
     * @param annotationStyle
     *            The style of annotations to use in the generated Java types.
     */
    public void setAnnotationStyle(AnnotationStyle annotationStyle) {
        this.annotationStyle = annotationStyle;
    }

    /**
     * Sets whether to use the title as classname.
     *
     * @param useTitleAsClassname
     *            When {@code true}, derive the classname from title.
     */
    public void setUseTitleAsClassname(boolean useTitleAsClassname) {
        this.useTitleAsClassname = useTitleAsClassname;
    }

    /**
     * Sets the 'inclusionLevel' property of this class
     *
     * @param inclusionLevel
     *            The level of inclusion for Jackson serializer.
     */
    public void setInclusionLevel(InclusionLevel inclusionLevel) {
        this.inclusionLevel = inclusionLevel;
    }

    /**
     * Sets the 'customAnnotator' property of this class
     *
     * @param customAnnotator
     *            A custom annotator to use to annotate the generated types
     */
    @SuppressWarnings("unchecked")
    public void setCustomAnnotator(String customAnnotator) {
        if (isNotBlank(customAnnotator)) {
            try {
                this.customAnnotator = (Class<? extends Annotator>) Class.forName(customAnnotator);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            this.customAnnotator = NoopAnnotator.class;
        }
    }

    @SuppressWarnings("unchecked")
    public void setCustomRuleFactory(String customRuleFactory) {
        if (isNotBlank(customRuleFactory)) {
            try {
                this.customRuleFactory = (Class<? extends RuleFactory>) Class.forName(customRuleFactory);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            this.customRuleFactory = RuleFactory.class;
        }
    }

    /**
     * Sets the 'includeJsr303Annotations' property of this class
     *
     * @param includeJsr303Annotations
     *            Whether to include
     *            <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a>
     *            annotations (for schema rules like minimum, maximum, etc) in
     *            generated Java types.
     */
    public void setIncludeJsr303Annotations(boolean includeJsr303Annotations) {
        this.includeJsr303Annotations = includeJsr303Annotations;
    }

    /**
     * Sets the 'includeJsr305Annotations' property of this class
     *
     * @param includeJsr305Annotations
     *            Whether to include
     *            <a href="http://jcp.org/en/jsr/detail?id=305">JSR-305</a>
     *            annotations (for schema rules like Nullable, NonNull, etc) in
     *            generated Java types.
     */
    public void setIncludeJsr305Annotations(boolean includeJsr305Annotations) {
        this.includeJsr305Annotations = includeJsr305Annotations;
    }


    /**
     * Sets the 'useOptionalForGetters' property of this class
     *
     * @param useOptionalForGetters
     *         Whether to use {@link java.util.Optional} as return type for
     *         getters of non-required fields.
     */
    public void setUseOptionalForGetters(boolean useOptionalForGetters) {
        this.useOptionalForGetters = useOptionalForGetters;
    }

    /**
     * Sets the 'sourceType' property of this class
     *
     * @param sourceType
     *            The type of input documents that will be read
     *            <p>
     *            Supported values:
     *            <ul>
     *            <li><code>jsonschema</code></li>
     *            <li><code>json</code></li>
     *            <li><code>yamlschema</code></li>
     *            <li><code>yaml</code></li>
     *            </ul>
     */
    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Sets the 'removeOldOutput' property of this class
     *
     * @param removeOldOutput
     *            Whether to empty the target directory before generation
     *            occurs, to clear out all source files that have been generated
     *            previously. <strong>Be warned</strong>, when activated this
     *            option will cause jsonschema2pojo to <strong>indiscriminately
     *            delete the entire contents of the target directory (all files
     *            and folders)</strong> before it begins generating sources.
     */
    public void setRemoveOldOutput(boolean removeOldOutput) {
        this.removeOldOutput = removeOldOutput;
    }

    /**
     * Sets the 'outputEncoding' property of this class
     *
     * @param outputEncoding
     *            The character encoding that should be used when writing the
     *            generated Java source files
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    /**
     * Sets the 'includeConstructorPropertiesAnnotation' property of this class
     *
     * @param includeConstructorPropertiesAnnotation Determines if constructors will be annotated with JDK-provided ConstructorProperties annotation. (Not Available on Android)
     */
    public void setIncludeConstructorPropertiesAnnotation(boolean includeConstructorPropertiesAnnotation) {
        this.includeConstructorPropertiesAnnotation = includeConstructorPropertiesAnnotation;
    }

    /**
     * Sets the 'useJodaDates' property of this class
     *
     * @param useJodaDates
     *            Whether to use {@link org.joda.time.DateTime} instead of
     *            {@link java.util.Date} when adding date type fields to
     *            generated Java types.
     */
    public void setUseJodaDates(boolean useJodaDates) {
        this.useJodaDates = useJodaDates;
    }

    /**
     * Sets the 'useJodaLocalDates' property of this class
     *
     * @param useJodaLocalDates
     *            Whether to use {@link org.joda.time.LocalDate} instead of
     *            string when adding string fields of format date (not
     *            date-time) to generated Java types.
     */
    public void setUseJodaLocalDates(boolean useJodaLocalDates) {
        this.useJodaLocalDates = useJodaLocalDates;
    }

    /**
     * Sets the 'useJodaLocalTimes' property of this class
     *
     * @param useJodaLocalTimes
     *            Whether to use {@link org.joda.time.LocalTime} instead of
     *            string when adding string fields of format time (not
     *            date-time) to generated Java types.
     */
    public void setUseJodaLocalTimes(boolean useJodaLocalTimes) {
        this.useJodaLocalTimes = useJodaLocalTimes;
    }

    /**
     * Sets the 'useCommonsLang3' property of this class
     *
     * @param useCommonsLang3
     *            whether to use commons-lang 3.x imports instead of commons-lang 2.x imports when adding equals, hashCode and toString methods
     * @deprecated no longer in use, please remove it from your config
     */
    @Deprecated
    public void setUseCommonsLang3(boolean useCommonsLang3) {
        super.log("useCommonsLang3 is deprecated. Please remove it from your config.", Project.MSG_WARN);
    }

    /**
     * Sets the 'parcelable' property of this class
     *
     * @param parcelable
     *            Whether to make the generated types 'parcelable' (for Android
     *            development).
     */
    public void setParcelable(boolean parcelable) {
        this.parcelable = parcelable;
    }

    /**
     * Sets the 'serializable' property of this class
     *
     * @param serializable
     *            Whether to make the generated types 'serializable'.
     */
    public void setSerializable(boolean serializable) {
        this.serializable = serializable;
    }

    /**
     * Sets the 'initializeCollections' property of this class
     *
     * @param initializeCollections
     *            Whether to initialize collections with empty instance or null.
     */
    public void setInitializeCollections(boolean initializeCollections) {
        this.initializeCollections = initializeCollections;
    }

    /**
     * Sets the 'classNamePrefix' property of this class
     *
     * @param classNamePrefix
     *            Whether to add a prefix to generated classes.
     */
    public void setClassNamePrefix(String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
    }

    /**
     * Sets the 'classNameSuffix' property of this class
     *
     * @param classNameSuffix
     *            Whether to add a suffix to generated classes.
     */
    public void setClassNameSuffix(String classNameSuffix) {
        this.classNameSuffix = classNameSuffix;
    }

    /**
     * Sets the 'fileExtensions' property of this class
     *
     * @param fileExtensions
     *            The array of strings that should be considered as file
     *            extensions and therefore not included in class names.
     */
    public void setFileExtensions(String[] fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    /**
     * Sets the 'includeAdditionalProperties' property of this class
     *
     * @param includeAdditionalProperties
     *            Whether to allow 'additional properties' support in objects.
     *            Setting this to false will disable additional properties
     *            support, regardless of the input schema(s).
     */
    public void setIncludeAdditionalProperties(boolean includeAdditionalProperties) {
        this.includeAdditionalProperties = includeAdditionalProperties;
    }

    /**
     * Sets the 'includeGetters' property of this class
     *
     * @param includeGetters
     *            Whether to include getters or to omit these accessor
     *            methods and create public fields instead.
     */
    public void setIncludeGetters(boolean includeGetters) {
        this.includeGetters = includeGetters;
    }

    /**
     * Sets the 'includeSetters' property of this class
     *
     * @param includeSetters
     *            Whether to include setters or to omit these accessor
     *            methods and create public fields instead.
     */
    public void setIncludeSetters(boolean includeSetters) {
        this.includeSetters = includeSetters;
    }

    /**
     * Sets the 'targetVersion' property of this class
     *
     * @param targetVersion
     *            The target version for generated source files.
     *            <p>
     *            Default: <code>1.6</code>.
     */
    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    /**
     * Sets the 'includeDynamicAccessors' property of this class
     *
     * @param includeDynamicAccessors
     *            Whether to include dynamic getters, setters, and builders or to omit these methods.
     */
    public void setIncludeDynamicAccessors(boolean includeDynamicAccessors) {
        this.includeDynamicAccessors = includeDynamicAccessors;
    }

    /**
     * Sets the 'includeDynamicGetters' property of this class
     *
     * @param includeDynamicGetters
     *            Whether to include dynamic getters or to omit these methods.
     */
    public void setIncludeDynamicGetters(boolean includeDynamicGetters) {
        this.includeDynamicGetters = includeDynamicGetters;
    }

    /**
     * Sets the 'includeDynamicSetters' property of this class
     *
     * @param includeDynamicSetters
     *            Whether to include dynamic setters or to omit these methods.
     */
    public void setIncludeDynamicSetters(boolean includeDynamicSetters) {
        this.includeDynamicSetters = includeDynamicSetters;
    }

    /**
     * Sets the 'includeDynamicBuilders' property of this class
     *
     * @param includeDynamicBuilders
     *            Whether to include dynamic builders or to omit these methods.
     */
    public void setIncludeDynamicBuilders(boolean includeDynamicBuilders) {
        this.includeDynamicBuilders = includeDynamicBuilders;
    }


    /**
     * Sets the 'formatDateTimes' property of this class
     *
     * @param formatDateTimes
     *            Whether the fields of type <code>date-type</code> have the <code>@JsonFormat</code> annotation
     *            with pattern set to the default value of <code>yyyy-MM-dd'T'HH:mm:ss.SSS</code>
     *            and timezone set to default value of `UTC`
     */
    public void setFormatDateTime(boolean formatDateTimes) {
        this.formatDateTimes = formatDateTimes;
    }

    /**
     * Sets the 'formatTimes' property of this class
     *
     * @param formatTimes
     *            Whether the fields of type <code>time</code> have the <code>@JsonFormat</code> annotation
     *            with pattern set to the default value of <code>HH:mm:ss.SSS</code>.
     */
    public void setFormatTimes(boolean formatTimes) {
        this.formatTimes = formatTimes;
    }

    /**
     * Sets the 'formatDates' property of this class
     *
     * @param formatDates
     *            Whether the fields of type <code>date</code> have the <code>@JsonFormat</code> annotation
     *            with pattern set to the default value of <code>yyyy-MM-dd</code>.
     */
    public void setFormatDates(boolean formatDates) {
        this.formatDates = formatDates;
    }

    /**
     * Sets the 'customDatePattern' property of this class
     *
     * @param customDatePattern
     *            A custom pattern to use when formatting date fields during
     *            serialization. Requires support from your JSON binding
     *            library.
     */
    public void setCustomDatePattern(String customDatePattern) {
        this.customDatePattern = customDatePattern;
    }

    /**
     * Sets the 'customTimePattern' property of this class
     *
     * @param customTimePattern
     *            A custom pattern to use when formatting time fields during
     *            serialization. Requires support from your JSON binding
     *            library.
     */
    public void setCustomTimePattern(String customTimePattern) {
        this.customTimePattern = customTimePattern;
    }

    /**
     * Sets the 'customDateTimePattern' property of this class
     *
     * @param customDateTimePattern
     *            A custom pattern to use when formatting date-time fields during
     *            serialization. Requires support from your JSON binding
     *            library.
     */
    public void setCustomDateTimePattern(String customDateTimePattern) {
        this.customDateTimePattern = customDateTimePattern;
    }

    /**
     * Sets the 'refFragmentPathDelimiters' property of this class
     *
     * @param refFragmentPathDelimiters A string containing any characters that should act as path delimiters when
     *                                  resolving $ref fragments. By default, #, / and . are used in an attempt
     *                                  to support JSON Pointer and JSON Path.
     */
    public void setRefFragmentPathDelimiters(String refFragmentPathDelimiters) {
        this.refFragmentPathDelimiters = refFragmentPathDelimiters;
    }

    /**
     * Sets the 'sourceSortOrder' property of this class
     *
     * @param sourceSortOrder Sets the sort order for the source files to be processed in.  By default the OS can
     *                        influence the processing order.
     */
    public void setSourceSortOrder(SourceSortOrder sourceSortOrder) {
        this.sourceSortOrder = sourceSortOrder;
    }

    /**
     * Sets the 'useInnerClassBuilders' property of this class
     *
     * @param useInnerClassBuilders determines whether builders will be chainable setters or embedded classes when {@link #isGenerateBuilders()} used
     */
    public void setUseInnerClassBuilders(boolean useInnerClassBuilders) {
        this.useInnerClassBuilders = useInnerClassBuilders;
    }

    /**
     * Sets the 'useJakartaValidation' property of this class
     *
     * @param useJakartaValidation Whether to use annotations from {@code jakarta.validation} package instead of {@code javax.validation} package
     *                             when adding <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a> annotations to generated Java types.
     */
    public void setUseJakartaValidation(boolean useJakartaValidation) {
        this.useJakartaValidation = useJakartaValidation;
    }

    public void setFormatTypeMapping(Map<String, String> formatTypeMapping) {
        this.formatTypeMapping = formatTypeMapping;
    }

    public void setFormatTypeMapping(String[] formatTypeMapping) {
        this.formatTypeMapping = Arrays.stream(formatTypeMapping)
            .collect(Collectors.toMap(m -> m.split(":")[0], m -> m.split(":")[1]));
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
    public boolean isUsePrimitives() {
        return usePrimitives;
    }

    @Override
    public Iterator<URL> getSource() {
        return Collections.singleton(URLUtil.parseURL(source)).iterator();
    }

    @Override
    public File getTargetDirectory() {
        return targetDirectory;
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    @Override
    public char[] getPropertyWordDelimiters() {
        return propertyWordDelimiters.clone();
    }

    /**
     * Should this task be skipped? (don't read schemas, don't generate types)
     *
     * @return <code>true</code> if this task is disabled
     */
    public boolean isSkip() {
        return skip;
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
        return annotationStyle;
    }

    @Override
    public boolean isUseTitleAsClassname() {
        return useTitleAsClassname;
    }

    @Override
    public InclusionLevel getInclusionLevel() {
        return inclusionLevel;
    }

    @Override
    public Class<? extends Annotator> getCustomAnnotator() {
        return customAnnotator;
    }

    @Override
    public Class<? extends RuleFactory> getCustomRuleFactory() {
        return customRuleFactory;
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
        return sourceType;
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public void setClasspathRef(Reference classpathRef) {
        createClasspath().setRefid(classpathRef);
    }

    public Path getClasspath() {
        return (classpath == null) ? new Path(getProject()) : classpath;
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
        return new AllFileFilter();
    }

    @Override
    public boolean isInitializeCollections() {
        return initializeCollections;
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
    public boolean isIncludeGetters() {
        return includeGetters;
    }

    @Override
    public boolean isIncludeSetters() {
        return includeSetters;
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
        return sourceSortOrder;
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
    public boolean isIncludeConstructorPropertiesAnnotation() {
        return includeConstructorPropertiesAnnotation;
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
