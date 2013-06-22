/**
 * Copyright ¬© 2010-2013 Nokia
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

package com.googlecode.jsonschema2pojo.integration.generation;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.compile;
import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.generate;
import static com.googlecode.jsonschema2pojo.integration.util.JsonAssert.assertEqualsJsonIgnoreAdditions;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper;
import com.googlecode.jsonschema2pojo.maven.Jsonschema2PojoMojo;

/**
 * <p>
 * Facilitates quick/easy integration tests. Given a schema and a set of
 * configuration values, by default this support class will:
 * </p>
 * <ol>
 * <li>Generate all output java files</li>
 * <li>Verify that every generated file was expected</li>
 * <li>Verify that every expected file was generated</li>
 * <li>Verify that every file generated contains EXACTLY what is expected</li>
 * <li>Verify marshalling/unmarshalling by performing a JSON roundtrip</li>
 * </ol>
 * In the event that any of these verifications fail, the support class makes a
 * solid effort to provide helpful information to resolve the issue. Also, it
 * tries to favor convention over configuration. Mainly, all input/output files
 * should follow the same file system patterns. This makes it much easier to
 * validate that, given a set of inputs, the integration test produces EXACTLY
 * the expected outputs.
 * 
 * @see com.googlecode.jsonschema2pojo.integration.util.
 *      CodeGenerationHelper#generate(java.net.URL, String, Map)
 * @author kgorham
 * 
 */
public abstract class GenerationTestSupport {

    // --------------------------------------------------------------------------
    //
    // Properties: Static
    //
    // --------------------------------------------------------------------------

    /**
     * The parent directory above all expected integration file directories. By
     * default, this is <code>src/test/resources/integration/</code>
     * 
     */
    protected static final String ROOT_RESOURCE_DIR = "src/test/resources"
	    + "/integration";

    /** Default configuration for all tests */
    protected static Map<String, Object> DEFAULT_CONFIG;
    static {
	resetDefaultConfig();
    }

    protected static final SimplePredicate<File> filesExist = new FilesExist();
    protected static final SimplePredicate<File> filesMatch = new FilesMatch();

    // --------------------------------------------------------------------------
    //
    // Properties: Initialized During setup()
    //
    // --------------------------------------------------------------------------

    /**
     * Populated with a list of all generated files during setup. Initialized
     * during setup.
     */
    protected static Collection<File> generatedFiles;

    /**
     * Populated with a list of all expected files during setup. Initialized
     * during setup.
     */
    protected static Collection<File> expectedFiles;

    /**
     * The temp output directory where generated files went during setup.
     * Initialized during setup.
     */
    protected static File outputDirectory;

    /**
     * <p>
     * The directory where expected files were placed. The default is:
     * <code>src/test/resources/[testSimpleName]/[schemaName]</code> Where
     * <code>[testSimpleName]</code> is the simpleName of the class under test
     * and <code>[schemaName]</code> is the filename of the schema, without the
     * extension, all lowercase.
     * </p>
     * <p>
     * Example: src/test/resources/output/GsonIT/person
     * </p>
     * <p>
     * Initialized during setup.
     * </p>
     */
    protected static File directoryWithExpectedFiles;

    /** Marker that helps determine when a new test is being executed */
    protected static Object previousInstanceIndicator;

    // --------------------------------------------------------------------------
    //
    // Generic Tests
    //
    // --------------------------------------------------------------------------

    /**
     * <p>
     * After setup generates java files, this test verifies that every file that
     * was expected, actually got created. By convention, expected files are
     * declared in
     * </p>
     * <p>
     * <code>src/test/resources/[testSimpleName]/[schemaName]</code>
     * </p>
     * <p>
     * Where <code>[testSimpleName]</code> is the simpleName of the class under
     * test and <code>[schemaName]</code> is the filename of the schema, without
     * the extension, all lowercase.
     * </p>
     */
    @Test
    public void expectedFilesWereCreatedTest() {
	String remedy = "To fix this, either determine why these expected"
		+ " files are not being generated or remove these"
		+ " files from this test's expected output directory: "
		+ directoryWithExpectedFiles + "/\n";

	verifyRelatedFiles(expectedFiles, directoryWithExpectedFiles,
		outputDirectory, filesExist,
		"Some files were expected but not generated", remedy);
    }

    /**
     * <p>
     * After setup generates java files, this test verifies that every file that
     * was created, actually was expected. By convention, expected files are
     * declared in
     * </p>
     * <p>
     * <code>src/test/resources/[testSimpleName]/[schemaName]</code>
     * </p>
     * <p>
     * Where <code>[testSimpleName]</code> is the simpleName of the class under
     * test and <code>[schemaName]</code> is the filename of the schema, without
     * the extension, all lowercase.
     * </p>
     */
    @Test
    public void createdFilesWereExpectedTest() {
	String remedy = "To fix this, either determine why these unexpected files are"
		+ " being generated or account for these files by creating them"
		+ " in the expected output directory for this test: "
		+ directoryWithExpectedFiles + "/\n";

	verifyRelatedFiles(generatedFiles, outputDirectory,
		directoryWithExpectedFiles, filesExist,
		"Some files were generated but not expected", remedy);
    }

    /**
     * <p>
     * After setup generates java files, this test verifies that every file
     * matches its expected counterpart, line-by-line. Expected files are
     * declared in:
     * </p>
     * <p>
     * <code>src/test/resources/[testSimpleName]/[schemaName]</code>
     * </p>
     * <p>
     * Where <code>[testSimpleName]</code> is the simpleName of the class under
     * test and <code>[schemaName]</code> is the filename of the schema, without
     * the extension, all lowercase.
     * </p>
     */
    @Test
    public void createdFilesContentExactlyMatchesExpectedFilesTest() {
	String remedy = "The diffs must be reconciled";

	verifyRelatedFiles(
		generatedFiles,
		outputDirectory,
		directoryWithExpectedFiles,
		filesMatch,
		"Some generated files did not exactly match what was expected:",
		remedy);
    }

    /**
     * <p>
     * By convention, this test assumes there is a sample JSON file that
     * corresponds to the schema file being tested. Schema files are expected
     * in:
     * </p>
     * <p>
     * <code>src/test/resources/integration/input/schema</code>
     * </p>
     * <p>
     * and the corresponding JSON samples are expected in:
     * </p>
     * <p>
     * <code>src/test/resources/integration/input/schema</code>
     * </p>
     * <p>
     * This Test Takes the example json, then:
     * </p>
     * <ol>
     * <li>unmarshals it into an object</li>
     * <li>writes that object out to json</li>
     * <li>compares this unmarshalled json to the original</li>
     * </ol>
     * 
     * <p>
     * For now, since adding "additionalProperties" is both supported by default
     * in Jsonschema2pojo and not optional, this test is forgiving for any
     * properties that are added to the original json. Besides, added properties
     * should not break most client contracts in "the real world," so this
     * "leniency" seems reasonable.
     * </p>
     */
    @Test
    public void roundTripJsonTest() throws Exception {
	Class<?> jsonExampleClass = null;
	try {
	    String jsonExample = getJsonExample();
	    jsonExampleClass = getJsonExampleClass();
	    Object unmarshalledResult = unmarshalExample(jsonExample,
		    jsonExampleClass);
	    String marshalledResult = marshalExample(unmarshalledResult,
		    jsonExampleClass);

	    assertEqualsJsonIgnoreAdditions(jsonExample, marshalledResult);
	} catch (Exception e) {
	    String msg = "ERROR [GenerationTestSupport.roundTripJsonTest] : error"
		    + " while attempting json roundtrip for "
		    + jsonExampleClass
		    + ". Verify that the sample file contains valid json (via"
		    + " jsonlint.com, for example). If it is valid, then verify"
		    + " that it matches the property types in the expected class.";
	    throw new RuntimeException(msg, e);
	}
    }

    // --------------------------------------------------------------------------
    //
    // Support Methods
    //
    // --------------------------------------------------------------------------

    /**
     * Creates expected file list and populates outputDirectory with generated
     * code. Configuration is reset between tests, to give parameterized tests
     * the option to use their own config, per test. A "new test" is defined as
     * one whose {@link #getTestInstanceId()} is different from the last one.
     */
    @Before
    public void setup() {
	// Shortcut to generate output once without using @BeforeClass, which
	// forces too many things to be static. Also provides a simple way to
	// reset things between parameterized tests, while also not
	// re-generating
	// code / expectedFiles needlessly between every unit test. -kg
	if (isNewTest()) {
	    resetDefaultConfig().putAll(getConfigValues());
	    createExpectedFiles();
	    createGeneratedFiles();
	}
    }

    /**
     * Checks the previous instance id to determine if the current one matches.
     * 
     * @return true when this instance id is different from the previous one.
     */
    protected boolean isNewTest() {
	if (previousInstanceIndicator == null
		|| !previousInstanceIndicator.equals(getTestInstanceId())) {
	    previousInstanceIndicator = getTestInstanceId();
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Clears the DEFAULT_CONFIG and reloads it with defaults
     * 
     * @return the DEFAULT_CONFIG map so that this method can be used fluidly.
     */
    private static Map<String, Object> resetDefaultConfig() {
	if (DEFAULT_CONFIG == null)
	    DEFAULT_CONFIG = new HashMap<String, Object>();
	else
	    DEFAULT_CONFIG.clear();

	DEFAULT_CONFIG.put("includeHashcodeAndEquals", false);
	DEFAULT_CONFIG.put("includeToString", false);
	return DEFAULT_CONFIG;
    }

    /**
     * Provides an Id that changes any time a new test instance is being run. By
     * default, this returns null which causes every test to recreate all
     * properties. However, subclasses can (and should) override this and return
     * a new value anytime new files should be generated.
     * 
     * @return the Id for this instance. Typically, this is the name of the
     *         schema being used or any other property that controls code
     *         generation. When new code should be generated, this method should
     *         return a new value.
     */
    protected Object getTestInstanceId() {
	// by default, always a new instance
	return null;
    }

    /**
     * Recurses through all files in the expected files directory and loads them
     * into the {@link #directoryWithExpectedFiles} property.
     */
    protected void createExpectedFiles() {
	directoryWithExpectedFiles = new File(
		getDirectoryNameForExpectedFiles());
	assertTrue("Parent directory for the expected files did not exist: "
		+ directoryWithExpectedFiles,
		directoryWithExpectedFiles.exists());
	expectedFiles = listFiles(directoryWithExpectedFiles, null, true);
	assertNotNull("The parent directory for the expcted files was invalid",
		expectedFiles);
    }

    /**
     * Runs code generation, through {@link CodeGenerationHelper} and sets the
     * resulting temp directory as the {@link #outputDirectory} property. Also,
     * recurses through all the generated files and loads them into the
     * {@link #generatedFiles} property.
     * 
     * @see #generateOutput()
     * @see CodeGenerationHelper#generate(String, String, Map)
     */
    protected void createGeneratedFiles() {
	outputDirectory = generateOutput();
	assertNotNull("Generated output directory was null.", outputDirectory);
	generatedFiles = listFiles(outputDirectory, null, true);
	assertNotNull("The output directory was invalid.", generatedFiles);
    }

    /**
     * Runs code generation, through {@link CodeGenerationHelper} and sets the
     * resulting temp directory as the {@link #outputDirectory} property.
     * 
     * @see CodeGenerationHelper#generate(String, String, Map)
     */
    protected File generateOutput() {
	String schema = getSchema();
	if (schema.startsWith("src/test/resources"))
	    schema = schema.replace("src/test/resources", "");
	return generate(schema, getDefaultPackage(), DEFAULT_CONFIG);
    }

    /**
     * For each file in <code>filesToCheck</code>, get the related file (based
     * on the <code>relatedDir</code>) and apply the given
     * <code>predicate</code> to both the file to check and the related file to
     * determine whether they are valid. Whenever the predicate returns false,
     * it's considered an error.
     * 
     * @param filesToCheck
     *            list of files to check against those in the related directory
     * @param filesToCheckRootDir
     *            the root directory of all files to check. Typically this is
     *            {@link #directoryWithExpectedFiles} or
     *            {@link #outputDirectory}.
     * @param relatedDir
     *            the root directory of all related files. Typically this is
     *            {@link #outputDirectory} or
     *            {@link #directoryWithExpectedFiles}.
     * @param predicate
     *            the predicate used to compare each file to its related file.
     *            Typically this is {@link #filesExist} or {@link #filesMatch}.
     * @param baseErrorMessage
     *            the error message to prepend to the final error output
     * @param remedy
     *            the remedy to append to the final erro output. The remedy
     *            gives a bit of guidance on troubleshooting the base error.
     */
    public void verifyRelatedFiles(Collection<File> filesToCheck,
	    File filesToCheckRootDir, File relatedDir,
	    SimplePredicate<File> predicate, String baseErrorMessage,
	    String remedy) {
	assertNotNull(
		baseErrorMessage
			+ "\n\tThere were no files to check!"
			+ " This most likely means that code generation completely failed.",
		filesToCheck);

	StringBuilder errorMessage = null;
	for (File fileToCheck : filesToCheck) {
	    File relatedFile = getRelatedFile(fileToCheck, filesToCheckRootDir,
		    relatedDir);
	    if (!predicate.apply(fileToCheck, relatedFile)) {
		if (errorMessage == null)
		    errorMessage = new StringBuilder(baseErrorMessage);
		errorMessage.append("\n\t").append(
			relativeName(fileToCheck, filesToCheckRootDir));
	    }
	}

	assertTrue(errorMessage + "\n\n" + remedy, errorMessage == null);
    }

    /**
     * Given a file, return the related file that it should correspond to. For
     * example a file named src/main/resources/samples/data.txt would have a
     * root directory of src/main/resources and you may expect to find a related
     * file under ./target with a path of ./target/samples/data.txt
     * 
     * @param file
     *            a file in a root directory that has other related files in a
     *            different directory.
     * @param fileRootDirectory
     *            the root directory of the provided file.
     * @param relatedDirectory
     *            the related directory in which similarly named files should
     *            exist.
     * @return the related file, based on the directory for related files
     */
    protected File getRelatedFile(File file, File fileRootDirectory,
	    File relatedDirectory) {
	String relativeName = relativeName(file, fileRootDirectory);
	String expectedFileName = relatedDirectory + "/" + relativeName;
	return new File(expectedFileName);
    }

    /**
     * @return the package plus the name of the unit test as the package.
     */
    protected String getDefaultPackage() {
	return getClass().getCanonicalName().toLowerCase();
    }

    /**
     * Get the directory name for expected files. Subclasses can override this
     * if they prefer to break convention and put expected files elsewhere.
     * 
     * @return {@link #DEFAULT_EXPECTED_FILE_ROOT_DIR} +
     *         <code>&lt;test_name&gt;</code>. Where <code>test_name</code> is
     *         the simpleName of the test subclass being executed.
     * 
     */
    protected String getDirectoryNameForExpectedFiles() {
	String schemaSimpleName = "";
	if (getSchema() != null)
	    schemaSimpleName = "/"
		    + getSimpleFileName(getSchema()).toLowerCase();
	return ROOT_RESOURCE_DIR + "/output/" + getClass().getSimpleName()
		+ schemaSimpleName;
    }

    /**
     * Given the {@link #getSchema()} value, return the expected json example
     * file name.
     * 
     * @return the expected json example file
     */
    protected String getJsonExampleFileName() {
	File schema = new File(getSchema());
	String jsonDirectoryName = // input/json instead of input/schema
	schema.getParentFile().getParentFile().getAbsolutePath() + "/json";
	String name = getRelatedFile(schema, schema.getParentFile(),
		new File(jsonDirectoryName)).getAbsolutePath();
	return name;
    }

    /**
     * Pulls in and returns the JSON example file as a string.
     * 
     * @return the contents of the JSON example file.
     */
    protected String getJsonExample() {
	File jsonExample = new File(getJsonExampleFileName());
	assertTrue("Example Json file did not exist: " + jsonExample,
		jsonExample.exists());
	try {
	    return FileUtils.readFileToString(jsonExample);
	} catch (IOException e) {
	    fail("Failed to read json example file as String due to: " + e);
	    return null; // dead code but it quiets the warning.
	}

    }

    /**
     * Get the generated Class object that can be used for unmarshalling the
     * sample json file.
     * 
     * @return Class that was created during setup, corresponding to the json
     *         under test.
     */
    protected Class<?> getJsonExampleClass() {
	assertTrue(
		"Failed to get example class because output directory"
			+ " is missing. This most likely means that code generation failed.",
		outputDirectory != null && outputDirectory.exists());

	ClassLoader classLoader = compile(outputDirectory,
		Arrays.asList(outputDirectory.getAbsolutePath()));
	String className = getSimpleFileName(getJsonExampleFileName());
	try {
	    String fullClassName = getDefaultPackage() + "." + className;
	    return classLoader.loadClass(fullClassName);
	} catch (ClassNotFoundException e) {
	    fail("Unable to load json example class : " + className);
	    return null; // dead code but it quiets the warning.
	}
    }

    /**
     * Returns the expected path to the schema file, given the value of
     * {@link #getSchemaFileName()}
     * 
     * @return the expected path to the schema file.
     */
    protected String getSchema() {
	return ROOT_RESOURCE_DIR + "/input/schema/" + getSchemaFileName();
    }

    // --------------------------------------------------------------------------
    //
    // Utility Methods: someday these can be moved to another utility class or
    // replaced altogether by library calls
    // --------------------------------------------------------------------------

    /**
     * Given a file name and a directory, return the base file name relative to
     * that directory.
     * 
     * @param file
     *            a file within a particular directory
     * @param directory
     *            directory containing the given file
     * @return the file name with the directory portion removed
     */
    protected String relativeName(File file, File directory) {
	if (file == null || directory == null)
	    return null;
	if (!file.getAbsolutePath().startsWith(directory.getAbsolutePath()))
	    throw new IllegalArgumentException(
		    "ERROR [GenerationTestSupport.java]: cannot return a "
			    + "file relative to a directory if that file is not in "
			    + "the directory.\n\tFile: "
			    + file.getAbsolutePath() + "\n\tDirectory: "
			    + directory.getAbsolutePath());

	return file.getAbsolutePath().substring(
		directory.getAbsolutePath().length() + 1);
    }

    /**
     * Given a file name such as <code>/var/logs/MyLog.out</code>, this method
     * returns its simple name: <code>MyLog</code>. In other words, this method
     * removes the parent directory and extension from a file name.
     * 
     * @param absolutePath
     *            absolute path to a file
     * @return the name of the file, minus the directory and extension
     */
    protected String getSimpleFileName(String absolutePath) {
	/*
	 * note this method could be relocated to a string utility or replaced
	 * by some library call
	 */
	if (absolutePath == null)
	    return "";

	String simpleName = absolutePath;
	if (simpleName.contains(File.separator)) {
	    simpleName = simpleName.substring(simpleName
		    .lastIndexOf(File.separator) + 1);
	}
	if (simpleName.contains("."))
	    simpleName = simpleName.substring(0, simpleName.lastIndexOf("."));

	return simpleName;
    }

    // --------------------------------------------------------------------------
    //
    // Predicates
    //
    // --------------------------------------------------------------------------

    /** Simple predicate interface */
    protected static interface SimplePredicate<T> {
	boolean apply(T... input);
    }

    /** Quick predicate to determine whether all files exist */
    protected static final class FilesExist implements SimplePredicate<File> {
	@Override
	public boolean apply(File... inputFiles) {
	    boolean allExist = true;
	    for (File f : inputFiles)
		allExist = allExist && (f != null && f.exists());
	    return allExist;
	}
    }

    /** Quick predicate to determine whether all files match */
    protected static final class FilesMatch implements SimplePredicate<File> {
	@Override
	public boolean apply(File... inputFiles) {
	    if (inputFiles.length != 2)
		return false;
	    try {
		// simple way to generate a diff UI in the final test results
		// -kg
		assertEquals("Some generated files did not match what was "
			+ "expected.\n\t\tGenerated file: " + inputFiles[0]
			+ "\n\t\tExpected file: " + inputFiles[1] + "\n",
			FileUtils.readFileToString(inputFiles[1]),
			FileUtils.readFileToString(inputFiles[0]));
		return true;
	    } catch (IOException e) {
		return false;
	    }
	}
    }

    // --------------------------------------------------------------------------
    //
    // Abstract Methods
    //
    // --------------------------------------------------------------------------

    /**
     * <p>
     * A map including any properties in the jsonschema2pojo maven plugin.
     * Subclasses can supply these values to influences code generation in tests
     * as expected. During test setup, this method is called once and all values
     * returned are added to the defaults.
     * </p>
     * 
     * Available properties include:
     * <ul>
     * <li>outputDirectory</li>
     * <li>sourceDirectory</li>
     * <li>sourcePaths</li>
     * <li>targetPackage</li>
     * <li>generateBuilders</li>
     * <li>usePrimitives</li>
     * <li>addCompileSourceRoot</li>
     * <li>skip</li>
     * <li>propertyWordDelimiters</li>
     * <li>useLongIntegers</li>
     * <li>includeHashcodeAndEquals</li>
     * <li>includeToString</li>
     * <li>annotationStyle</li>
     * <li>customAnnotator</li>
     * <li>includeJsr303Annotations</li>
     * <li>sourceType</li>
     * <li>removeOldOutput</li>
     * </ul>
     * 
     * @see Jsonschema2PojoMojo
     */
    protected abstract Map<String, Object> getConfigValues();

    /**
     * Get the file name for the Schema file to use for testing. Example:
     * Person.json
     * 
     * @return the file name of the Schema file to use for testing.
     */
    protected abstract String getSchemaFileName();

    /**
     * Marshal the given object out to a string and return it.
     * 
     * @param unmarshalledResult
     *            the object to marshal
     * @param jsonExampleClass
     *            the class of the object to marshal
     * @return the resulting string
     */
    protected abstract String marshalExample(Object unmarshalledResult,
	    Class<?> jsonExampleClass);

    /**
     * Unmrashal the given json into an Object of the specified type.
     * 
     * @param jsonExample
     *            sample json string to unmarshal.
     * @param jsonExampleClass
     *            the class to unmarshal into.
     * @return the unmarshalled object.
     */
    protected abstract <T> T unmarshalExample(String jsonExample,
	    Class<T> jsonExampleClass);

}
