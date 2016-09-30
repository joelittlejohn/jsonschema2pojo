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

package org.jsonschema2pojo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;
import org.apache.commons.lang3.text.WordUtils;
import org.jsonschema2pojo.GenerationConfig;
import java.util.Stack;

import static java.lang.Character.isDigit;
import static javax.lang.model.SourceVersion.isKeyword;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.join;

public class NameHelper {

    public static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

    private final GenerationConfig generationConfig;

    private final Stack<String> nodeContext = new Stack<String>();

    public NameHelper(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    public String replaceIllegalCharacters(String name) {
        return name.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
    }

    public String normalizeName(String name) {
        name = capitalizeTrailingWords(name);

        if (isDigit(name.charAt(0))) {
            name = "_" + name;
        }

        return name;
    }

    public String capitalizeTrailingWords(String name) {
        char[] wordDelimiters = generationConfig.getPropertyWordDelimiters();

        if (containsAny(name, wordDelimiters)) {
            String capitalizedNodeName = WordUtils.capitalize(name, wordDelimiters);
            name = name.charAt(0) + capitalizedNodeName.substring(1);

            for (char c : wordDelimiters) {
                name = remove(name, c);
            }
        }

        return name;
    }

    private String makeLowerCamelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Convert jsonFieldName into the equivalent Java fieldname by replacing
     * illegal characters and normalizing it.
     * 
     * @param jsonFieldName
     * @param node
     * @return
     */
    public String getPropertyName(String jsonFieldName, JsonNode node) {
        jsonFieldName = getFieldName(jsonFieldName, node);

        jsonFieldName = replaceIllegalCharacters(jsonFieldName);
        jsonFieldName = normalizeName(jsonFieldName);
        jsonFieldName = makeLowerCamelCase(jsonFieldName);

        if (isKeyword(jsonFieldName)) {
            jsonFieldName = "_" + jsonFieldName;
        }

        if (isKeyword(jsonFieldName)) {
            jsonFieldName += "_";
        }

        return jsonFieldName;
    }

    /**
     * Generate setter method name for property.
     * 
     * @param propertyName
     * @param node
     * @return
     */
    public String getSetterName(String propertyName, JsonNode node) {
        propertyName = getFieldName(propertyName, node);

        propertyName = replaceIllegalCharacters(propertyName);
        String setterName = "set" + capitalize(capitalizeTrailingWords(propertyName));

        if (setterName.equals("setClass")) {
            setterName = "setClass_";
        }

        return setterName;
    }

    /**
     * Get name of the field generated from property.
     *
     * @param propertyName
     * @param node
     * @return
     */
    public String getFieldName(String propertyName, JsonNode node) {
        
        if (node != null && node.has("javaName")) {
            propertyName = node.get("javaName").textValue();
        }

        return propertyName;
    }

    /**
     * Generate getter method name for property.
     * 
     * @param propertyName
     * @param type
     * @param node
     * @return
     */
    public String getGetterName(String propertyName, JType type, JsonNode node) {
        propertyName = getFieldName(propertyName, node);

        String prefix = type.equals(type.owner()._ref(boolean.class)) ? "is" : "get";
        propertyName = replaceIllegalCharacters(propertyName);
        String getterName = prefix + capitalize(capitalizeTrailingWords(propertyName));

        if (getterName.equals("getClass")) {
            getterName = "getClass_";
        }

        return getterName;
    }

    /**
     * Generates a class name prefix based on the current nodeContext
     *
     * @param prefix Any already defined class name prefix
     * @param delimiter A delimiter separating the contextual parts of the class name prefix
     *
     * @return The class name prefix
     */
    public String getContextualClassPrefix(String prefix, String delimiter) {
        if (nodeContext.size() > 0) {
            prefix += (prefix.length() > 0 ? delimiter : "") + join(nodeContext, delimiter) + delimiter;
        }
        return prefix;
    }

    /**
     * Generates a sub-package name based on the current nodeContext
     *
     * @return The sub-package name
     */
    public String getContextualSubPackage() {
        return nodeContext.size() > 0 ? nodeContext.get(nodeContext.size() - 1).toLowerCase() : "";
    }

    /**
     * Pushes a node to the nodeContext.
     *
     * @param nodeName The name of the node
     */
    public void pushToNodeContext(String nodeName) {
        nodeContext.push(capitalize(capitalizeTrailingWords(nodeName)));
    }

    /**
     * Gets and removes the lastly pushed node from the nodeContext.
     *
     * @return The name of the removed node
     */
    public String popFromNodeContext() {
        return nodeContext.pop();
    }
}
