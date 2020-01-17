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

package org.jsonschema2pojo;

/**
 * The level of inclusion to be set for generated java types (to control level of inclusion in case of Jackson1 or
 * Jackson2 serializers). Each level will have its own set of mapping annotations.
 *
 * @see <a href="http://fasterxml.github.io/jackson-annotations/javadoc/2.7/com/fasterxml/jackson/annotation/JsonInclude.Include.html" >JsonInclude.Include</a>
 */
public enum InclusionLevel {

    /**
     * Value that indicates that property is to be always included, independent of value of the property.
     * Valid for both versions of Jackson.
     */
    ALWAYS,

    /**
     * Value that indicates that properties are included unless their value is:
     * <ul>
     * <li>null
     * <li>"absent" value of a referential type (like Java 8 `Optional`, or
     * {link java.utl.concurrent.atomic.AtomicReference}); that is, something
     * that would not deference to a non-null value.
     * </ul>
     * Valid only for jackson2, for Jackson1 NON_NUL is used.
     */
    NON_ABSENT,

    /**
     * Meaning of this setting depends on context: whether annotation is specified for POJO type (class), or not
     * Valid for both versions of Jackson.
     */
    NON_DEFAULT,

    /**
     * Value that indicates that only properties with null value, or what is considered empty, are not to be included.
     * Valid for both versions of Jackson.
     */
    NON_EMPTY,

    /**
     * Value that indicates that only properties with non-null values are to be included.
     * Valid for both versions of Jackson.
     */
    NON_NULL,

    /**
     * Pseudo-value used to indicate that the higher-level defaults make sense,
     * to avoid overriding inclusion value. Valid only for jackson2, for
     * Jackson1 NON_NULL is used.
     */
    USE_DEFAULTS,
}
