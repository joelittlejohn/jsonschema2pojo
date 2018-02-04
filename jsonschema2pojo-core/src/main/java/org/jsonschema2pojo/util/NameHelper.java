/**
 * Copyright © 2010-2017 Nokia
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

import static java.lang.Character.*;
import static javax.lang.model.SourceVersion.*;
import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.text.WordUtils;
import org.jsonschema2pojo.GenerationConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

public class NameHelper {

    public static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

    private final GenerationConfig generationConfig;

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
        return toLowerCase(name.charAt(0)) + name.substring(1);
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
        propertyName = getPropertyNameForAccessor(propertyName, node);
        
        String prefix = "set";

        String setterName;
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            setterName = prefix + propertyName;
        } else {
            setterName = prefix + capitalize(propertyName);
        }

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
        propertyName = getPropertyNameForAccessor(propertyName, node);
        
        String prefix = type.equals(type.owner()._ref(boolean.class)) ? "is" : "get";

        String getterName;
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            getterName = prefix + propertyName;
        } else {
            getterName = prefix + capitalize(propertyName);
        }

        if (getterName.equals("getClass")) {
            getterName = "getClass_";
        }

        return getterName;
    }
    
    private String getPropertyNameForAccessor(String jsonPropertyName, JsonNode node) {
        jsonPropertyName = getFieldName(jsonPropertyName, node);
        jsonPropertyName = replaceIllegalCharacters(jsonPropertyName);
        jsonPropertyName = capitalizeTrailingWords(jsonPropertyName);
        return jsonPropertyName;
    }
}
