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

package org.jsonschema2pojo.rules;

import static java.lang.Character.*;
import static javax.lang.model.SourceVersion.isKeyword;
import static org.apache.commons.lang3.StringUtils.*;

import com.sun.codemodel.JType;
import org.apache.commons.lang3.text.WordUtils;

import org.jsonschema2pojo.GenerationConfig;

public class NameHelper {

    private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

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

    /**
     * Convert jsonFieldName into the equivalent Java fieldname by replacing illegal characters and normalizing it.
     * @param jsonFieldName
     * @return
     */
    public String getPropertyName(String jsonFieldName) {
        jsonFieldName = replaceIllegalCharacters(jsonFieldName);
        jsonFieldName = normalizeName(jsonFieldName);

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
     * @param propertyName
     * @return
     */
    public String getSetterName(String propertyName) {
        propertyName = replaceIllegalCharacters(propertyName);
        String setterName = "set" + capitalize(capitalizeTrailingWords(propertyName));

        if (setterName.equals("setClass")) {
            setterName = "setClass_";
        }

        return setterName;
    }


    /**
     * Generate getter method name for property.
     * @param propertyName
     * @param type
     * @return
     */
    public String getGetterName(String propertyName, JType type) {
        String prefix = type.equals(type.owner()._ref(boolean.class)) ? "is" : "get";
        propertyName = replaceIllegalCharacters(propertyName);
        String getterName = prefix + capitalize(capitalizeTrailingWords(propertyName));

        if (getterName.equals("getClass")) {
            getterName = "getClass_";
        }

        return getterName;
    }
}
