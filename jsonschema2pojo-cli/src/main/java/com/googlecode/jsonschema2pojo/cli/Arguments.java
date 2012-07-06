/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.cli;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.googlecode.jsonschema2pojo.GenerationConfig;

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

    @Parameter(names = { "-s", "--sourceDirectory" }, description = "The source file(s) or directory(ies) from which JSON Schema will be read", required = true, converter = FileConverter.class)
    private List<File> sourceDirectory;

    @Parameter(names = { "-b", "--generate-builders" }, description = "Generate builder-style methods as well as setters")
    private boolean generateBuilderMethods = false;

    @Parameter(names = { "-P", "--use-primitives" }, description = "Use primitives instead of wrapper types for bean properties")
    private boolean usePrimitives = false;

    @Parameter(names = { "-d", "--word-delimiters" }, description = "The characters that should be considered as word delimiters when creating Java Bean property names from JSON property names")
    private String propertyWordDelimiters;

    @Parameter(names = { "-l", "--long-integers" }, description = "Use long (or Long) instead of int (or Integer) when the JSON Schema type 'integer' is encountered")
    private boolean useLongIntegers = false;

    @Parameter(names = { "-E", "--omit-hashcode-and-equals" }, description = "Omit hashCode and equals methods in the generated Java types")
    private boolean omitHashcodeAndEquals = false;

    @Parameter(names = { "-S", "--omit-tostring" }, description = "Omit the toString method in the generated Java types")
    private boolean omitToString = false;

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
	    return sourceDirectory.iterator();
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
    public boolean isIncludeHashcodeAndEquals() {
        return !omitHashcodeAndEquals;
    }

    @Override
    public boolean isIncludeToString() {
        return !omitToString;
    }

    protected void exit(int status) {
        System.exit(status);
    }

}
