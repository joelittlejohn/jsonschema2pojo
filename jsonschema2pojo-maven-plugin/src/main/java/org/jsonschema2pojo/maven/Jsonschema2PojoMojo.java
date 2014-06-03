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

package org.jsonschema2pojo.maven;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

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

/**
 * When invoked, this goal reads one or more <a
 * href="http://json-schema.org/">JSON Schema</a> documents and generates DTO
 * style Java classes for data binding.
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @threadSafe
 * @see <a
 *      href="http://maven.apache.org/developers/mojo-api-specification.html">Mojo
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
    private File sourceDirectory;

    /**
     * An array of locations of the JSON Schema file(s). Note: each item may
     * refer to a single file or a directory of files.
     * 
     * @parameter expression="${jsonschema2pojo.sourcePaths}"
     * @since 0.3.1
     */
    private File[] sourcePaths;

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
     * Whether to generate a static inner Builder class for each generated
     * Java type.
     *
     * @parameter expression="${jsonschema2pojo.generateBuilderClasses}"
     *            default-value="false"
     */
    private boolean generateBuilderClasses = false;

    /**
     * Whether to generate Java types that are immutable.
     *
     * @parameter expression="${jsonschema2pojo.immutable}"
     *            default-value="false"
     */
    private boolean immutable = false;

    /**
     * Whether to generate public fields for properties instead of getters
     * and setters.
     *
     * @parameter expression="${jsonschema2pojo.usePublicFields}"
     *            default-value="false"
     */
    private boolean usePublicFields = false;

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
     * <li>
     * <code>jackson2</code> (apply annotations from the <a
     * href="https://github.com/FasterXML/jackson-annotations">Jackson 2.x</a>
     * library)</li>
     * <li>
     * <code>jackson1</code> (apply annotations from the <a
     * href="http://jackson.codehaus.org/">Jackson 1.x</a> library)</li>
     * <li>
     * <code>gson</code> (apply annotations from the <a
     * href="https://code.google.com/p/google-gson/">gson</a> library)</li>
     * <li>
     * <code>none</code> (apply no annotations at all)</li>
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
     * Whether to include <a
     * href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a> annotations (for
     * schema rules like minimum, maximum, etc) in generated Java types.
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
     *            default="false"
     * @since 0.3.7
     */
    private boolean removeOldOutput = false;

    /**
     * The character encoding that should be used when writing the generated
     * Java source files.
     * 
     * @parameter expression="${jsonschema2pojo.outputEncoding}" default="UTF-8"
     * @since 0.4.0
     */
    private String outputEncoding = "UTF-8";

    /**
     * Whether to use {@link org.joda.time.DateTime} instead of
     * {@link java.util.Date} when adding date type fields to generated Java
     * types.
     * 
     * @parameter expression="${jsonschema2pojo.useJodaDates}" default="false"
     * @since 0.4.0
     */
    private boolean useJodaDates = false;

    /**
     * Whether to use commons-lang 3.x imports instead of commons-lang 2.x
     * imports when adding equals, hashCode and toString methods.
     * 
     * @parameter expression="${jsonschema2pojo.useCommonsLang3}"
     *            default="false"
     * @since 0.4.1
     */
    private boolean useCommonsLang3 = false;

    /**
     * List of file patterns to include.
     * 
     * @parameter
     * @since 0.4.3
     */
    private String[] includes;

    /**
     * List of file patterns to exclude.
     * 
     * @parameter
     * @since 0.4.3
     */
    private String[] excludes;

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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = {
            "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "Private fields set by Maven.")
    public void execute() throws MojoExecutionException {

        try {
            getAnnotationStyle();
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Not a valid annotation style: " + annotationStyle);
        }

        try {
            new AnnotatorFactory().getAnnotator(getCustomAnnotator());
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (skip) {
            return;
        }

        if (null == sourceDirectory && null == sourcePaths) {
            throw new MojoExecutionException("One of sourceDirectory or sourcePaths must be provided");
        }

        if (filteringEnabled()) {

            if (sourceDirectory == null) {
                throw new MojoExecutionException("Source includes and excludes require the sourceDirectory property");
            }

            fileFilter = createFileFilter();
        }

        if (addCompileSourceRoot) {
            project.addCompileSourceRoot(outputDirectory.getPath());
        }

        addProjectDependenciesToClasspath();

        try {
            Jsonschema2Pojo.generate(this);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Error generating classes from JSON Schema file(s) " + sourceDirectory.getPath(), e);
        }

    }

    private void addProjectDependenciesToClasspath() {

        try {

            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
            Thread.currentThread().setContextClassLoader(newClassLoader);

        } catch (DependencyResolutionRequiredException e) {
            getLog().info(
                    "Skipping addition of project artifacts, there appears to be a dependecy resolution problem",
                    e);
        }

    }

    @Override
    public boolean isGenerateBuilders() {
        return generateBuilders;
    }

    @Override
    public boolean isGenerateBuilderClasses() {
        return generateBuilderClasses;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public boolean isUsePublicFields() {
        return usePublicFields;
    }

    @Override
    public File getTargetDirectory() {
        return outputDirectory;
    }

    @Override
    public Iterator<File> getSource() {
        if (null != sourceDirectory) {
            return Collections.singleton(sourceDirectory).iterator();
        }
        return Arrays.asList(sourcePaths).iterator();
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
    public boolean isIncludeJsr303Annotations() {
        return includeJsr303Annotations;
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
    public boolean isUseCommonsLang3() {
        return useCommonsLang3;
    }

    @Override
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    boolean filteringEnabled() {
        return !((includes == null || includes.length == 0) && (excludes == null || excludes.length == 0));
    }

    FileFilter createFileFilter() throws MojoExecutionException {
        try {
            return new MatchPatternsFileFilter.Builder()
                    .addIncludes(includes)
                    .addExcludes(excludes)
                    .addDefaultExcludes()
                    .withSourceDirectory(sourceDirectory.getCanonicalPath())
                    .withCaseSensitive(false)
                    .build();
        } catch (IOException e) {
            throw new MojoExecutionException("could not create file filter", e);
        }
    }
}
