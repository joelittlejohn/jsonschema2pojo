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

package org.jsonschema2pojo.rules;

import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JPrimitiveType;

/**
 * Helper for checking and creating primitive type references during code
 * generation.
 */
public final class PrimitiveTypes {

    private PrimitiveTypes() {
    }

    /**
     * Check if a name string refers to a given type.
     *
     * @param name
     *            the name of a Java type
     * @param owner
     *            the current code model for type generation
     * @return <code>true</code> when the given name refers to a primitive Java
     *         type (e.g. "int"), otherwise <code>false</code>
     */
    public static boolean isPrimitive(String name, JCodeModel owner) {
        try {
            return AbstractJType.parse(owner, name) != owner.VOID;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Create a primitive type reference (for code generation) using the given
     * primitive type name.
     *
     * @param name
     *            the name of a primitive Java type
     * @param owner
     *            the current code model for type generation
     * @return a type reference created by the given owner
     */
    public static JPrimitiveType primitiveType(String name, JCodeModel owner) {
        return (JPrimitiveType) owner.parseType(name);
    }

}
