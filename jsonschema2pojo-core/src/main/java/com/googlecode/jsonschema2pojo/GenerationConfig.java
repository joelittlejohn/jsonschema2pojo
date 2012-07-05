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

package com.googlecode.jsonschema2pojo;

import java.io.File;
import java.util.Iterator;

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
	 * @return The source file(s) or directory(ies) from which JSON Schema will be read.
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
	 * @return Whether to use the java type {@link long} (or
	 *         {@link java.lang.Long}) instead of {@link int} (or
	 *         {@link java.lang.Integer}) when representing the JSON Schema type
	 *         'integer'.
	 */
	boolean isUseLongIntegers();

}
