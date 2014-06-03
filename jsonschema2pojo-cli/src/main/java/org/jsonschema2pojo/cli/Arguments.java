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

package org.jsonschema2pojo.cli;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;

import org.jsonschema2pojo.AllFileFilter;
import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.SourceType;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

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

    @Parameter(names = { "-s", "--source" }, description = "The source file(s) or directory(ies) from which JSON Schema will be read", required = true, converter = FileConverter.class)
    private List<File> sourcePaths;

    @Parameter(names = { "-b", "--generate-builders" }, description = "Generate builder-style methods as well as setters")
    private boolean generateBuilderMethods = false;

    @Parameter(names = { "-B", "--generate-builder-classes" }, description = "Generate inner Builder classes for generated Java types")
    private boolean generateBuilderClasses = false;

    @Parameter(names = { "-i", "--immutable" }, description = "Generate immutable Java types")
    private boolean immutable = false;

    @Parameter(names = { "-u", "--use-public-fields" }, description = "Use public fields for properties instead of getters and setters")
    private boolean usePublicFields = false;

    @Parameter(names = { "-P", "--use-primitives" }, description = "Use primitives instead of wrapper types for bean properties")
    private boolean usePrimitives = false;

    @Parameter(names = { "-d", "--word-delimiters" }, description = "The characters that should be considered as word delimiters when creating Java Bean property names from JSON property names")
    private String propertyWordDelimiters = "- _";

    @Parameter(names = { "-l", "--long-integers" }, description = "Use long (or Long) instead of int (or Integer) when the JSON Schema type 'integer' is encountered")
    private boolean useLongIntegers = false;

    @Parameter(names = { "-f", "--float-numbers" }, description = "Use float (or Float) instead of double (or Double) when the JSON Schema type 'number' is encountered")
    private boolean useFloatNumbers = false;

    @Parameter(names = { "-E", "--omit-hashcode-and-equals" }, description = "Omit hashCode and equals methods in the generated Java types")
    private boolean omitHashcodeAndEquals = false;

    @Parameter(names = { "-S", "--omit-tostring" }, description = "Omit the toString method in the generated Java types")
    private boolean omitToString = false;

    @Parameter(names = { "-a", "--annotation-style" })
    private AnnotationStyle annotationStyle = AnnotationStyle.JACKSON;

    @Parameter(names = { "-A", "--custom-annotator" }, description = "The fully qualified class name of referring to a custom annotator class that implements org.jsonschema2pojo.Annotator " +
            "and will be used in addition to the --annotation-style. If you want to use a custom annotator alone, set --annotation-style to none",
            converter = ClassConverter.class)
    private Class<? extends Annotator> customAnnotator = NoopAnnotator.class;

    @Parameter(names = { "-303", "--jsr303-annotations" }, description = "Add JSR-303 annotations to generated Java types.")
    private boolean includeJsr303Annotations = false;

    @Parameter(names = { "-T", "--source-type" })
    private SourceType sourceType = SourceType.JSONSCHEMA;

    @Parameter(names = { "-R", "--remove-old-output" }, description = "Whether to empty the target directory before generation occurs, to clear out all source files that have been generated previously (indiscriminately deletes all files and folders).")
    private boolean removeOldOutput = false;

    @Parameter(names = { "-e", "--output-encoding" }, description = "The character encoding that should be used when writing the generated Java source files.")
    private String outputEncoding = "UTF-8";

    @Parameter(names = { "-j", "--joda-dates" }, description = "Whether to use org.joda.time.DateTime instead of java.util.Date when adding date type fields to generated Java types.")
    private boolean useJodaDates = false;

    @Parameter(names = { "-c3", "--commons-lang3" }, description = "Whether to use commons-lang 3.x imports instead of commons-lang 2.x imports when adding equals, hashCode and toString methods.")
    private boolean useCommonsLang3 = false;

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
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jCommander.usage();
            exit(EXIT_ERROR);
        }

        return this;
    }

    @Override
    public Iterator<File> getSource() {
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
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public boolean isGenerateBuilderClasses() {
        return generateBuilderClasses;
    }

    @Override
    public boolean isUsePublicFields() {
        return usePublicFields;
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
    public AnnotationStyle getAnnotationStyle() {
        return annotationStyle;
    }

    @Override
    public Class<? extends Annotator> getCustomAnnotator() {
        return customAnnotator;
    }

    @Override
    public boolean isIncludeJsr303Annotations() {
        return includeJsr303Annotations;
    }

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
    public boolean isUseCommonsLang3() {
        return useCommonsLang3;
    }
    
    protected void exit(int status) {
        System.exit(status);
    }

    @Override
    public FileFilter getFileFilter() {
        return new AllFileFilter();
    }

}
