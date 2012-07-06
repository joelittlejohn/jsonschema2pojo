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

package com.googlecode.jsonschema2pojo.ant;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.googlecode.jsonschema2pojo.GenerationConfig;
import com.googlecode.jsonschema2pojo.cli.Jsonschema2Pojo;
import com.googlecode.jsonschema2pojo.cli.SingleFileIterator;

/**
 * When invoked, this task reads one or more <a
 * href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 * <p>
 * See <a href=
 * 'http://jsonschema2pojo.googlecode.com'>jsonschema2pojo.googlecode.com</a>.
 * 
 * @see <a href="http://ant.apache.org/manual/develop.html">Writing Your Own
 *      Task</a>
 */
public class Jsonschema2PojoTask extends Task implements GenerationConfig {

    private boolean generateBuilders;

    private boolean usePrimitives;

    private File source;

    private File targetDirectory;

    private String targetPackage;

    private boolean skip;

    private char[] propertyWordDelimiters = new char[] {};

    private boolean useLongIntegers;

    private boolean includeHashcodeAndEquals = true;

    private boolean includeToString = true;

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

        if (!source.exists()) {
            log(source.getAbsolutePath() + " cannot be found");
            return;
        }

        if (targetDirectory == null) {
            log("targetDirectory attribute is required but was not set");
            return;
        }

        try {
            Jsonschema2Pojo.generate(this);
        } catch (IOException e) {
            throw new BuildException(
                    "Error generating classes from JSON Schema file(s) "
                            + source.getPath(), e);
        }
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
     *            Whether to use the java type {@link long} (or
     *            {@link java.lang.Long}) instead of {@link int} (or
     *            {@link java.lang.Integer}) when representing the JSON Schema
     *            type 'integer'.
     */
    public void setUseLongIntegers(boolean useLongIntegers) {
        this.useLongIntegers = useLongIntegers;
    }

    /**
     * Sets schema file (or directory containing schema files) that should be
     * used for input.
     * 
     * @param source
     *            Location of the JSON Schema file(s). Note: this may refer to a
     *            single file or a directory of files.
     */
    public void setSource(File source) {
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
        this.propertyWordDelimiters = defaultString(propertyWordDelimiters)
                .toCharArray();
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

    @Override
    public boolean isGenerateBuilders() {
        return generateBuilders;
    }

    @Override
    public boolean isUsePrimitives() {
        return usePrimitives;
    }

    @Override
    public Iterator<File> getSource() {
        return new SingleFileIterator(source);
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
        return propertyWordDelimiters;
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
    public boolean isIncludeHashcodeAndEquals() {
        return includeHashcodeAndEquals;
    }

    @Override
    public boolean isIncludeToString() {
        return includeToString;
    }

}