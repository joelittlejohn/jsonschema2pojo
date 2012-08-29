/**
 * Copyright ¬© 2010-2011 Nokia
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

import static java.lang.Character.isDigit;
import static org.apache.commons.lang.StringUtils.containsAny;
import static org.apache.commons.lang.StringUtils.remove;

import org.apache.commons.lang.WordUtils;

public class NameHelper {
    private RuleFactory ruleFactory;
    
    public NameHelper(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }
    
    public String normalizeName(String nodeName) {
        nodeName = capitalizeTrailingWords(nodeName);
        
        if (isDigit(nodeName.charAt(0))) {
            nodeName = "_" + nodeName;
        }

        return nodeName;
    }
    
    public String capitalizeTrailingWords(String nodeName) {
        char[] wordDelimiters = ruleFactory.getGenerationConfig().getPropertyWordDelimiters();

        if (containsAny(nodeName, wordDelimiters)) {
            String capitalizedNodeName = WordUtils.capitalize(nodeName, wordDelimiters);
            nodeName = nodeName.charAt(0) + capitalizedNodeName.substring(1);

            for (char c : wordDelimiters) {
                nodeName = remove(nodeName, c);
            }
        }

        return nodeName;
    }
}
