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

package org.jsonschema2pojo.cli;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.jsonschema2pojo.AllFileFilter;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.InclusionLevel;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SourceSortOrder;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.cli.CommandLineLogger.LogLevelValidator;
import org.jsonschema2pojo.rules.RuleFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Describes and parses the command line arguments supported by the
 * jsonschema2pojo CLI.
 */
public class Arguments implements GenerationConfig {

    @Parameter(names = { "-h", "--help" }, description = "Print help information and exit")
    private boolean showHelp = false;

    @Parameter(names = { "-p", "--package" }, description = "A java package used for generated types")
    private String targetPackage;

    @Parameter(names = { "-t", "--target" }, description = "The target directory into which generated types will be written", required = true)
    private File targetDirectory;

    @Parameter(names = { "-s", "--source" }, description = "The source file(s) or directory(ies) from which JSON Schema will be read", required = true, variableArity = true, converter = UrlConverter.class)
    private List<URL> sourcePaths;

    @Parameter(names = { "-b", "--generate-builders" }, description = "Generate builder-style methods as well as setters")
    private boolean generateBuilderMethods = false;

    @Parameter(names = { "--include-type-info" }, description = "Include json type info; required to support polymorphic type handling. https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization")
    private boolean includeTypeInfo = false;

    @Parameter(names = { "--use-inner-class-builders" }, description = "Generate an inner class with builder-style methods")
    private boolean useInnerClassBuilders = false;

    @Parameter(names = { "--include-constructor-properties-annotation" }, description = "Generate ConstructorProperties annotation with parameter names of constructors. (Not Available on Android)")
    private boolean includeConstructorPropertiesAnnotation = false;

    @Parameter(names = { "-c", "--generate-constructors" }, description = "Generate constructors")
    private boolean generateConstructors = false;

    @Parameter(names = { "-r", "--constructors-required-only" }, description = "Generate only a constructor with only required fields")
    private boolean constructorsRequiredPropertiesOnly = false;

    @Parameter(names = { "--constructors-include-required-properties-constructor" }, description = "Generate a constructor with only required fields")
    private boolean includeRequiredPropertiesConstructor = false;

    @Parameter(names = { "--constructors-include-all-properties-constructor" }, description = "Generate a constructor with all fields")
    private boolean includeAllPropertiesConstructor = true;

    @Parameter(names = { "--constructors-include-copy-constructor" }, description = "Generate constructors with a copy oriented parameter")
    private boolean includeCopyConstructor = false;

    @Parameter(names = { "-P", "--use-primitives" }, description = "Use primitives instead of wrapper types for bean properties")
    private boolean usePrimitives = false;

    @Parameter(names = { "-d", "--word-delimiters" }, description = "The characters that should be considered as word delimiters when creating Java Bean property names from JSON property names")
    private String propertyWordDelimiters = "- _";

    @Parameter(names = { "-l", "--long-integers" }, description = "Use long (or Long) instead of int (or Integer) when the JSON Schema type 'integer' is encountered")
    private boolean useLongIntegers = false;

    @Parameter(names = { "-bi", "--big-integers" }, description = "Use BigInteger instead of int (or Integer) when the JSON Schema type 'integer' is encountered. Note that this overrides -l/--long-integers")
    private boolean useBigIntegers = false;

    @Parameter(names = { "-f", "--float-numbers" }, description = "Use float (or Float) instead of double (or Double) when the JSON Schema type 'number' is encountered")
    private boolean useFloatNumbers = false;

    @Parameter(names = { "-i", "--big-decimals" }, description = "Use BigDecimal instead of double (or Double) when the JSON Schema type 'number' is encountered. Note that this overrides -f/--float-numbers")
    private boolean useBigDecimals = false;

    @Parameter(names = { "-E", "--omit-hashcode-and-equals" }, description = "Omit hashCode and equals methods in the generated Java types")
    private boolean omitHashcodeAndEquals = false;

    @Parameter(names = { "-S", "--omit-tostring" }, description = "Omit the toString method in the generated Java types")
    private boolean omitToString = false;
    
    @Parameter(names = { "-tse", "--tostring-excludes" }, description = "The fields that should be excluded from generated toString methods")
    private String toStringExcludes = "";

    @Parameter(names = { "-a", "--annotation-style" })
    private AnnotationStyle annotationStyle = AnnotationStyle.JACKSON;

    @Parameter(names = { "-ut", "--use-title-as-classname", "When set class names are generated from title attributes rather than property names." })
    private boolean useTitleAsClassname = false;

    @Parameter(names = {"-il", "--inclusion-level"})
    private InclusionLevel inclusionLevel = InclusionLevel.NON_NULL;

    @Parameter(names = { "-A", "--custom-annotator" }, description = "The fully qualified class name of referring to a custom annotator class that implements org.jsonschema2pojo.Annotator " + "and will be used in addition to the --annotation-style. If you want to use a custom annotator alone, set --annotation-style to none", converter = ClassConverter.class)
    private Class<? extends Annotator> customAnnotator = NoopAnnotator.class;

    @Parameter(names = { "-F", "--custom-rule-factory" }, description = "The fully qualified class name of referring to a custom rule factory class that extends org.jsonschema2pojo.rules.RuleFactory " + "to create custom rules for code generation.", converter = ClassConverter.class)
    private Class<? extends RuleFactory> customRuleFactory = RuleFactory.class;

    @Parameter(names = { "-303", "--jsr303-annotations" }, description = "Add JSR-303/349 annotations to generated Java types.")
    private boolean includeJsr303Annotations = false;

    @Parameter(names = { "-305", "--jsr305-annotations" }, description = "Add JSR-305 annotations to generated Java types.")
    private boolean includeJsr305Annotations = false;

    @Parameter(names = { "-o", "--use-optional-for-getters"}, description = "Use Optional for getters of non-required fields.")
    private boolean useOptionalForGetters = false;

    @Parameter(names = { "-T", "--source-type" })
    private SourceType sourceType = SourceType.JSONSCHEMA;

    @Parameter(names = { "-R", "--remove-old-output" }, description = "Whether to empty the target directory before generation occurs, to clear out all source files that have been generated previously (indiscriminately deletes all files and folders).")
    private boolean removeOldOutput = false;

    @Parameter(names = { "-e", "--output-encoding" }, description = "The character encoding that should be used when writing the generated Java source files.")
    private String outputEncoding = "UTF-8";

    @Parameter(names = { "-j", "--joda-dates" }, description = "Whether to use org.joda.time.DateTime instead of java" + ".util.Date when adding date-time type fields to generated Java types.")
    private boolean useJodaDates = false;

    @Parameter(names = { "-jd", "--joda-local-dates" }, description = "Whether to use org.joda.time.LocalDate instead" + "of String when adding date type fields to generated Java types.")
    private boolean useJodaLocalDates = false;

    @Parameter(names = { "-jt", "--joda-local-times" }, description = "Whether to use org.joda.time.LocalTime instead" + "of String when adding time type fields to generated Java types.")
    private boolean useJodaLocalTimes = false;

    @Parameter(names = { "-dtt", "--datetime-class" }, description = "Specify datetime class")
    private String dateTimeType;

    @Parameter(names = { "-tt", "--time-class" }, description = "Specify time class")
    private String timeType;

    @Parameter(names = { "-dt", "--date-class" }, description = "Specify date class")
    private String dateType;

    @Parameter(names = { "-pl", "--parcelable" }, description = "**EXPERIMENTAL** Whether to make the generated types 'parcelable' (for Android development).")
    private boolean parcelable = false;

    @Parameter(names = { "-sl", "--serializable" }, description = "Whether to make the generated types 'serializable'.")
    private boolean serializable = false;

    @Parameter(names = { "-N", "--null-collections" }, description = "Initialize Set and List fields to null instead of an empty collection.")
    private boolean nullCollections = false;

    @Parameter(names = { "-y", "--class-prefix" }, description = "Prefix for generated class.")
    private String classNamePrefix = "";

    @Parameter(names = { "-x", "--class-suffix" }, description = "Suffix for generated class.")
    private String classNameSuffix = "";

    @Parameter(names = { "-fe", "--file-extensions" }, description = "The extensions that should be considered as standard filename extensions when creating java class names.")
    private String fileExtensions = "";

    @Parameter(names = { "-D", "--enable-additional-properties" }, description = "Enable additional properties support on generated types, regardless of the input schema(s)")
    private boolean isIncludeAdditionalProperties = false;

    @Parameter(names = { "-dg", "--disable-getters" }, description = "Whether to omit getter methods and create public fields instead.")
    private boolean disableGetters = false;

    @Parameter(names = { "-ds", "--disable-setters" }, description = "Whether to omit setter methods and create public fields instead.")
    private boolean disableSetters = false;

    @Parameter(names = { "-tv", "--target-version" }, description = "The target version for generated source files.")
    private String targetVersion = "8";

    @Parameter(names = { "-ida", "--include-dynamic-accessors" }, description = "Include dynamic getter, setter, and builder support on generated types.")
    private boolean includeDynamicAccessors = false;

    @Parameter(names = { "-idg", "--include-dynamic-getters" }, description = "Include dynamic getter support on generated types.")
    private boolean includeDynamicGetters = false;

    @Parameter(names = { "-ids", "--include-dynamic-setters" }, description = "Include dynamic setter support on generated types.")
    private boolean includeDynamicSetters = false;

    @Parameter(names = { "-idb", "--include-dynamic-builders" }, description = "Include dynamic builder support on generated types.")
    private boolean includeDynamicBuilders = false;

    @Parameter(names = { "-fd", "--format-dates" }, description = "Whether the fields of type `date` are formatted during serialization with a default pattern of `yyyy-MM-dd`")
    private boolean formatDates = false;

    @Parameter(names = { "-ft", "--format-times" }, description = "Whether the fields of type `time` are formatted during serialization with a default pattern of `HH:mm:ss.SSS`")
    private boolean formatTimes = false;

    @Parameter(names = { "-fdt", "--format-date-times" }, description = "Whether the fields of type `date-time` are formatted during serialization with a default pattern of `yyyy-MM-dd'T'HH:mm:ss.SSSZ` and timezone set to default value of `UTC`")
    private boolean formatDateTimes = false;

    @Parameter(names = { "-dp", "--date-pattern" }, description = "A custom pattern to use when formatting date fields during serialization")
    private String customDatePattern;

    @Parameter(names = { "-tp", "--time-pattern" }, description = "A custom pattern to use when formatting time fields during serialization")
    private String customTimePattern;

    @Parameter(names = { "-dtp", "--date-time-pattern" }, description = "A custom pattern to use when formatting date-time fields during serialization")
    private String customDateTimePattern;

    @Parameter(names = {"-rpd", "--ref-fragment-path-delimiters"}, description = "A string containing any characters that should act as path delimiters when resolving $ref fragments. By default, #, / and . are used in an attempt to support JSON Pointer and JSON Path.")
    private String refFragmentPathDelimiters = "#/.";

    @Parameter(names = { "-sso", "--source-sort-order" }, description = "The sort order to be applied to the source files.  Available options are: OS, FILES_FIRST or SUBDIRS_FIRST")
    private SourceSortOrder sourceSortOrder = SourceSortOrder.OS;

    @Parameter(names = { "-ftm", "--format-type-mapping" }, description = "Mapping from format identifier to type: <format>:<fully.qualified.Type>.", variableArity = true)
    private List<String> formatTypeMapping = new ArrayList<>();

    @Parameter(names = { "-log" }, description = "Configure log level. Defaults to info. Available options are: off, error, warn, info, debug, trace", validateWith = LogLevelValidator.class )
    private String logLevel = CommandLineLogger.DEFAULT_LOG_LEVEL;

    @Parameter(names = {"--print-log-levels"}, description = "Prints available log levels and exit.")
    private boolean printLogLevels = false;

    @Parameter(names = {"--omit-generated-annotation"}, description = "Omit @Generated annotation on generated types")
    private boolean omitGeneratedAnnotation = false;

    @Parameter(names = { "--useJakartaValidation" }, description = "Whether to use annotations from jakarta.validation package instead of javax.validation package when adding JSR-303/349 annotations to generated Java types")
    private boolean useJakartaValidation = false;

    @Parameter(names = { "-v", "--version"}, description = "Print version information", help = true)
    private boolean printVersion = false;

    private static final int EXIT_OKAY = 0;
    private static final int EXIT_ERROR = 1;

    /**
     * Parses command line arguments and populates this command line instance.
     * <p>
     * If the command line arguments include the "help" argument, or if the
     * arguments have incorrect values or order, then usage information is
     * printed to {@link System#out} and the program terminates.
     *
     * @param args
     *            the command line arguments
     * @return an instance of the parsed arguments object
     */
    public Arguments parse(String[] args) {

        JCommander jCommander = new JCommander(this);
        jCommander.setProgramName("jsonschema2pojo");

        try {
            jCommander.parse(args);

            if (this.showHelp) {
                jCommander.usage();
                exit(EXIT_OKAY);
            } else if (printVersion) {
                Properties properties = new Properties();
                properties.load(getClass().getResourceAsStream("version.properties"));
                jCommander.getConsole().println(jCommander.getProgramName() + " version " + properties.getProperty("version"));
                exit(EXIT_OKAY);
            }
        } catch (IOException | ParameterException e) {
            System.err.println(e.getMessage());
            jCommander.usage();
            exit(EXIT_ERROR);
        }

        return this;
    }

    @Override
    public Iterator<URL> getSource() {
        return sourcePaths.iterator();
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
    public boolean isGenerateBuilders() {
        return generateBuilderMethods;
    }

    @Override
    public boolean isIncludeTypeInfo()
    {
        return includeTypeInfo;
    }

    public String getLogLevel() {
        return logLevel;
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
    public boolean isUsePrimitives() {
        return usePrimitives;
    }

    @Override
    public char[] getPropertyWordDelimiters() {
        return defaultString(propertyWordDelimiters).toCharArray();
    }

    @Override
    public boolean isUseLongIntegers() {
        return useLongIntegers;
    }

    @Override
    public boolean isUseDoubleNumbers() {
        return !useFloatNumbers;
    }

    @Override
    public boolean isIncludeHashcodeAndEquals() {
        return !omitHashcodeAndEquals;
    }

    @Override
    public boolean isIncludeToString() {
        return !omitToString;
    }

    @Override
    public String[] getToStringExcludes() {
        return defaultString(toStringExcludes).split(" ");
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
    public boolean isUseOptionalForGetters() { return useOptionalForGetters; }

    @Override
    public SourceType getSourceType() {
        return sourceType;
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

    protected void exit(int status) {
        System.exit(status);
    }

    @Override
    public FileFilter getFileFilter() {
        return new AllFileFilter();
    }

    @Override
    public boolean isInitializeCollections() {
        return !nullCollections;
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
        return defaultString(fileExtensions).split(" ");
    }

    @Override
    public boolean isIncludeConstructors() {
        return generateConstructors;
    }

    @Override
    public boolean isConstructorsRequiredPropertiesOnly() {
        return constructorsRequiredPropertiesOnly;
    }

    public boolean isPrintLogLevels() {
        return printLogLevels;
    }

    @Override
    public boolean isIncludeRequiredPropertiesConstructor() { return includeRequiredPropertiesConstructor; }

    @Override
    public boolean isIncludeAllPropertiesConstructor() { return includeAllPropertiesConstructor; }

    @Override
    public boolean isIncludeCopyConstructor() { return includeCopyConstructor; }

    @Override
    public boolean isIncludeAdditionalProperties() {
        return isIncludeAdditionalProperties;
    }

    @Override
    public boolean isIncludeGetters() {
        return !disableGetters;
    }

    @Override
    public boolean isIncludeSetters() {
        return !disableSetters;
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
    public String getRefFragmentPathDelimiters() {
        return refFragmentPathDelimiters;
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
    public SourceSortOrder getSourceSortOrder() {
        return sourceSortOrder;
    }

    @Override
    public Map<String, String> getFormatTypeMapping() {
        return formatTypeMapping
                .stream()
                .collect(Collectors.toMap(m -> m.split(":")[0], m -> m.split(":")[1]));
    }

    @Override
    public boolean isIncludeGeneratedAnnotation() {
        return !omitGeneratedAnnotation;
    }

    @Override
    public boolean isUseJakartaValidation() {
        return useJakartaValidation;
    }
}
