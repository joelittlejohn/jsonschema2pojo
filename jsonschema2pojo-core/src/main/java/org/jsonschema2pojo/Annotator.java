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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Adds annotations to generated types for compatibility with a JSON
 * serialization library.
 * <p>
 * Annotators that need the generation configuration should add a constructor
 * with {@link GenerationConfig} arg. Annotators that don't need the
 * configuration need only add a default constructor.
 */
public interface Annotator {

    /**
     * Add the necessary annotation to dictate correct property order during
     * serialization
     *
     * @param clazz
     *            a generated pojo class, that is serialized to JSON
     * @param propertiesNode
     *            the properties to be ordered
     */
    void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode);

    /**
     * Add the necessary annotation to cause only non-null values to be included
     * during serialization.
     *
     * @param clazz
     *            a generated pojo class, that is serialized to JSON
     * @param schema
     *            the object schema associated with this clazz
     */
    void propertyInclusion(JDefinedClass clazz, JsonNode schema);

    /**
     * Add the necessary annotation to mark a Java field as a JSON property
     *
     * @param field
     *            the field that contains data that will be serialized
     * @param clazz
     *            the owner of the field (class to which the field belongs)
     * @param propertyName
     *            the name of the JSON property that this field represents
     * @param propertyNode
     *            the schema node defining this property
     */
    void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode);

    /**
     * Add the necessary annotation to mark a Java method as the getter for a
     * JSON property
     *
     * @param getter
     *            the method that will be used to get the value of the given
     *            JSON property
     * @param propertyName
     *            the name of the JSON property that this getter gets
     */
    void propertyGetter(JMethod getter, String propertyName);

    /**
     * Add the necessary annotation to mark a Java method as the setter for a
     * JSON property
     *
     * @param setter
     *            the method that will be used to set the value of the given
     *            JSON property
     * @param propertyName
     *            the name of the JSON property that this setter sets
     */
    void propertySetter(JMethod setter, String propertyName);

    /**
     * Add the necessary annotation to mark a Java method as the getter for
     * additional JSON property values that do not match any of the other
     * property names found in the bean.
     *
     * @param getter
     *            the method that will be used to get the values of additional
     *            properties
     */
    void anyGetter(JMethod getter);

    /**
     * Add the necessary annotation to mark a Java method as the setter for
     * additional JSON property values that do not match any of the other
     * property names found in the bean.
     *
     * @param setter
     *            the method that will be used to set the values of additional
     *            properties
     */
    void anySetter(JMethod setter);

    /**
     * Add the necessary annotation to mark a static Java method as the
     * creator/factory method which can choose the correct Java enum value for a
     * given JSON value during deserialization.
     *
     * @param creatorMethod
     *            the method that can create a Java enum value from a JSON value
     */
    void enumCreatorMethod(JMethod creatorMethod);

    /**
     * Add the necessary annotation to mark a Java method as the value method
     * that is used to turn a Java enum value into a JSON value during
     * serialization.
     *
     * @param valueMethod
     *            the enum instance method that can create a JSON value during
     *            serialization
     */
    void enumValueMethod(JMethod valueMethod);

    /**
     * Add the necessary annotations to an enum constant. For instance, to force
     * the the given value to be used when serializing.
     */
    void enumConstant(JEnumConstant constant, String value);

    /**
     * Indicates whether the annotation style that this annotator uses can
     * support the JSON Schema 'additionalProperties' feature. In other words,
     * can the deserializer associated with this annotation style gather
     * unexpected, additional json properties and does it expect to include them
     * somewhere in the target Java instance.
     * <p>
     * Jackson is able to use it's <code>JsonAnyGetter</code> and
     * <code>JsonAnySetter</code> features for this purpose, hence for Jackson
     * annotators, this method will return <code>true</code>. Gson does not
     * support 'additional' property values (they are silently discarded at
     * deserialization time), hence for Gson annotators, this method would
     * return <code>false</code>. Moshi 1.x behaves similar to Gson and therefore
     * returns <code>false</code>.
     *
     * @return Whether this annotator has any way to support 'additional
     *         properties'.
     */
    boolean isAdditionalPropertiesSupported();

    void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName);
}
