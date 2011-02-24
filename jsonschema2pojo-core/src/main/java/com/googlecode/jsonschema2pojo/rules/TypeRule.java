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

import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.Schema;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Applies the schema rules that represent a property definition.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.1</a>
 */
public class TypeRule implements SchemaRule<JClassContainer, JType> {

    private static final String DEFAULT_TYPE_NAME = "any";

    private final RuleFactory ruleFactory;

    protected TypeRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When applied, this rule reads the details of the given node to determine
     * the appropriate Java type to return. This may be a newly generated type,
     * it may be a primitive type or other type such as {@link java.lang.String}
     * or {@link java.lang.Object}.
     * <p>
     * JSON schema types and their Java type equivalent:
     * <ul>
     * <li>"type":"any" => {@link java.lang.Object}
     * <li>"type":"array" => Either {@link java.util.Set} or
     * <li>"type":"boolean" => <code>boolean</code>
     * <li>"type":"integer" => <code>int</code>
     * <li>"type":"null" => {@link java.lang.Object}
     * <li>"type":"number" => <code>double</code>
     * <li>"type":"object" => Generated type (see {@link ObjectRule})
     * {@link java.util.List}, see {@link ArrayRule}
     * <li>"type":"string" => {@link java.lang.String} (or alternative based on
     * presence of "format", see {@link FormatRule})
     * </ul>
     * 
     * @param nodeName
     *            the name of the node for which this "type" rule applies
     * @param node
     *            the node for which this "type" rule applies
     * @param jClassContainer
     *            the package into which any newly generated type may be placed
     * @return the Java type which, after reading the details of the given
     *         schema node, most appropriately matches the "type" specified
     * @throws GenerationException
     *             if the type value found is not recognised.
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema schema) {

        String propertyTypeName = node.has("type") ? node.get("type").getTextValue() : DEFAULT_TYPE_NAME;

        JType type;

        if (propertyTypeName.equals("string")) {

            type = jClassContainer.owner().ref(String.class);
        } else if (propertyTypeName.equals("number")) {

            type = jClassContainer.owner().DOUBLE;
        } else if (propertyTypeName.equals("integer")) {

            type = jClassContainer.owner().INT;
        } else if (propertyTypeName.equals("boolean")) {

            type = jClassContainer.owner().BOOLEAN;
        } else if (propertyTypeName.equals("object")) {

            type = ruleFactory.getObjectRule().apply(nodeName, node, jClassContainer.getPackage(), schema);
        } else if (propertyTypeName.equals("array")) {

            type = ruleFactory.getArrayRule().apply(nodeName, node, jClassContainer.getPackage(), schema);
        } else if (propertyTypeName.equals("null")) {

            type = jClassContainer.owner().ref(Object.class);
        } else if (propertyTypeName.equals("any")) {

            type = jClassContainer.owner().ref(Object.class);
        } else {

            type = jClassContainer.owner().ref(Object.class);
        }

        if (node.has("format")) {
            type = ruleFactory.getFormatRule().apply(nodeName, node.get("format"), type, schema);
        }

        return type;
    }

}
