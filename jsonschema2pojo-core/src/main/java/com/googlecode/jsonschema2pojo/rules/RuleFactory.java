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

package com.googlecode.jsonschema2pojo.rules;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * Provides factory methods used to create code generation rules.
 */
public interface RuleFactory {

    /**
     * Behaviour properties key determining whether or not to include the
     * builder-style setters, i.e. "withFoo(foo)"
     */
    String GENERATE_BUILDERS_PROPERTY = "include-builders";

    /**
     * Gets from a key-value pair which defines some aspect of the behaviour of
     * the rules this factory creates.
     * 
     * @param key
     *            The name of the property.
     * @return The value of the property.
     */
    String getBehaviourProperty(String key);

    /**
     * Provides a rule instance that should be applied when an "array"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "array" declaration.
     */
    SchemaRule<JPackage, JClass> getArrayRule();

    /**
     * Provides a rule instance that should be applied when a "description"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "description" declaration.
     */
    SchemaRule<JDocCommentable, JDocComment> getDescriptionRule();

    /**
     * Provides a rule instance that should be applied when a "title"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "title" declaration.
     */
    SchemaRule<JDocCommentable, JDocComment> getTitleRule();

    /**
     * Provides a rule instance that should be applied when an "enum"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "enum" declaration.
     */
    SchemaRule<JClassContainer, JDefinedClass> getEnumRule();

    /**
     * Provides a rule instance that should be applied when a "format"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "format" declaration.
     */
    SchemaRule<JType, JType> getFormatRule();

    /**
     * Provides a rule instance that should be applied when an "object"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "object" declaration.
     */
    SchemaRule<JPackage, JDefinedClass> getObjectRule();

    /**
     * Provides a rule instance that should be applied when an "optional"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "optional" declaration.
     * @deprecated This rule is deprecated since version 03 of the draft spec.
     *             Schemas should declare required fields as "required" rather
     *             than optional fields as "optional"
     */
    @Deprecated
    SchemaRule<JDocCommentable, JDocComment> getOptionalRule();

    /**
     * Provides a rule instance that should be applied when a "required"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "required" declaration.
     */
    SchemaRule<JDocCommentable, JDocComment> getRequiredRule();

    /**
     * Provides a rule instance that should be applied when a "properties"
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "properties" declaration.
     */
    SchemaRule<JDefinedClass, JDefinedClass> getPropertiesRule();

    /**
     * Provides a rule instance that should be applied when a property
     * declaration (child of the "properties" declaration) is found in the
     * schema.
     * 
     * @return a schema rule that can handle a property declaration.
     */
    SchemaRule<JDefinedClass, JDefinedClass> getPropertyRule();

    /**
     * Provides a rule instance that should be applied to a node to find its
     * equivalent Java type. Typically invoked for properties, arrays, etc for
     * which a Java type must be found/generated.
     * 
     * @return a schema rule that can find/generate the relevant Java type for a
     *         given schema node.
     */
    SchemaRule<JClassContainer, JType> getTypeRule();

    /**
     * Provides a rule instance that should be applied when an
     * "additionalProperties" declaration is found in the schema.
     * 
     * @return a schema rule that can handle the "additionalProperties"
     *         declaration.
     */
    SchemaRule<JDefinedClass, JDefinedClass> getAdditionalPropertiesRule();

    /**
     * Provides a rule instance that should be applied when an schema
     * declaration is found in the schema.
     * 
     * @return a schema rule that can handle a schema declaration.
     */
    SchemaRule<JClassContainer, JType> getSchemaRule();

    /**
     * Provides a rule instance that should be applied when an property
     * declaration is found in the schema to assign any appropriate default
     * value to that property.
     * 
     * @return a schema rule that can handle the "default" declaration.
     */
    SchemaRule<JFieldVar, JFieldVar> getDefaultRule();

}
