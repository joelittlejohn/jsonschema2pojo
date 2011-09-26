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

/**
 * Defines the configuration options for Java type generation, including source
 * and target paths/packages and all behavioural options (e.g should builders be
 * generated, should primitives be used, etc).
 * <p>
 * Devs: add to this interface if you need to introduce a new config property.
 */
public interface GenerationConfig {

    /**
     * @return Whether to generate builder-style methods of the form
     *         <code>withXxx(value)</code> (that return <code>this</code>),
     *         alongside the standard, void-return setters.
     */
    boolean isGenerateBuilders();

    /**
     * @return whether to use primitives (<code>long</code>, <code>double</code>
     *         , <code>boolean</code>) instead of wrapper types where possible
     *         when generating bean properties (has the side-effect of making
     *         those properties non-null).
     */
    boolean isUsePrimitives();

    /**
     * @return The source file or directory from which JSON Schema will be read
     */
    File getSource();

    /**
     * @return The target directory into which generated types will be written
     *         (may or may not exist before types are written)
     */
    File getTargetDirectory();

    /**
     * @return The java package used for generated types.
     */
    String getTargetPackage();

}
