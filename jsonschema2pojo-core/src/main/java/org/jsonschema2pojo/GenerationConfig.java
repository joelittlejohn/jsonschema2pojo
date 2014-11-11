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

package org.jsonschema2pojo;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
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
    Iterator<File> getSource();

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
     * Gets the 'useDoubleNumbers' configuration option.
     * 
     * @return Whether to use the java type <code>double</code> (or
     *         {@link java.lang.Double}) instead of <code>float</code> (or
     *         {@link java.lang.Float}) when representing the JSON Schema type
     *         'number'.
     */
    boolean isUseDoubleNumbers();

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
     * Gets the 'annotationStyle' configuration option.
     * 
     * @return The style of annotations to use in the generated Java types.
     *         <p>
     *         Supported values:
     *         <ul>
     *         <li>
     *         <code>jackson1</code> (apply annotations from the <a
     *         href="http://jackson.codehaus.org/">Jackson 1.x</a> library)</li>
     *         <li>
     *         <code>jackson2</code> (apply annotations from the <a
     *         href="https://github.com/FasterXML/jackson-annotations">Jackson
     *         2.x</a> library)</li>
     *         <li>
     *         <code>gson</code> (apply annotations from the <a
     *         href="https://code.google.com/p/google-gson/">gson</a> library)</li>
     *         <li>
     *         <code>none</code> (apply no annotations at all)</li>
     *         </ul>
     * @see AnnotatorFactory
     */
    AnnotationStyle getAnnotationStyle();

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
     * @return An Rule Factory that will be used for the creation of generation rules.
     */
    Class<? extends RuleFactory> getCustomRuleFactory();

    /**
     * Gets the 'includeJsr303Annotations' configuration option.
     * 
     * @return Whether to include <a
     *         href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a>
     *         annotations (for schema rules like minimum, maximum, etc) in
     *         generated Java types.
     */
    boolean isIncludeJsr303Annotations();

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
     * Gets the 'useCommonsLang3' configuration option.
     * 
     * @return Whether to use commons-lang 3.x imports instead of commons-lang
     *         2.x imports when adding equals, hashCode and toString methods.
     */
    boolean isUseCommonsLang3();
    
    /**
     * Gets the file filter used to isolate the schema mapping files in the source directories.
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
     * @return Whether to initialize collections with empty instance or null.
     */
    String getClassNamePrefix();
    
    /**
     * Gets the 'getClassNameSuffix' configuration option.
     *
     * @return Whether to initialize collections with empty instance or null.
     */
    String getClassNameSuffix();
}
