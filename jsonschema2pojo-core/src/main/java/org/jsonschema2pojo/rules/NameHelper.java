/**
 * Copyright © 2010-2013 Nokia
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
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
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

    public String normalizeName(String name, boolean isClass) {
        name = capitalizeTrailingWords(name);

        if (isDigit(name.charAt(0))) {
            name = "_" + name;
        }

        // Class names should start with uppercase letters,
        // while property names should start with lovercase (see issue #129)
        name = 
            	(isClass ? toUpperCase(name.charAt(0)) : toLowerCase(name.charAt(0))) 
            	+ (name.length() > 1 ? name.substring(1) : "");

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
    
    public String packageNameForSchemaPath( String basePackage, String path ) {
        String[] segments = FilenameUtils
          .getFullPathNoEndSeparator(FilenameUtils.normalize(path))
          .split(Pattern.quote(File.separator));
        
        StringBuilder sb = new StringBuilder();
        if( basePackage != null && !"".equals(basePackage) ) {
            sb.append(basePackage);
        }
        for( String segment : segments ) {
            if( !segment.equals("") ) {
              sb.append(".").append(replaceIllegalCharacters(segment));
            }
        }
        return sb.toString();
    }

    public String nodeNameForUri(URI id) {
        return substringBeforeLast(id.getPath().replaceAll(".*?([^/]*)$", "$1"), ".");
    }
}
