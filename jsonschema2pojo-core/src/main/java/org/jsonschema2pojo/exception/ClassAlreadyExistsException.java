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

package org.jsonschema2pojo.exception;

import com.sun.codemodel.JType;

/**
 * Thrown to indicate that an attempt to create a new class failed, because a
 * class of the same name already exists (either on the classpath or in the
 * current map of types to be generated.
 */
public class ClassAlreadyExistsException extends Exception {

    private static final long serialVersionUID = 7694477714975772317L;
    
    private final JType existingClass;

    /**
     * Creates a new exception where the given existing class was found to
     * conflict with an attempt to create a new class.
     * 
     * @param existingClass
     *            the class already present on the classpath (or in the map of
     *            classes to be generated) when attempt to create a new class
     *            was made.
     */
    public ClassAlreadyExistsException(JType existingClass) {
        this.existingClass = existingClass;
    }

    /**
     * Gets the corresponding existing class that caused this exception.
     * 
     * @return the class already present on the classpath (or in the map of
     *         classes to be generated) when attempt to create a new class was
     *         made.
     */
    public JType getExistingClass() {
        return existingClass;
    }

}
