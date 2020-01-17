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

package org.jsonschema2pojo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeUniqueClassName {

    private static final Pattern UNIQUE_NAMING_PATTERN = Pattern.compile("(^.+__)(\\d+)$");
    
    /**
     * When the class name is not unique we will use two underscore '__' and a digit representing the number of time
     * this class was found
     */
    public static String makeUnique(String className) {
        
        final Matcher m = UNIQUE_NAMING_PATTERN.matcher(className);
        
        if (m.matches()) {
            // get the current number
            final Integer number = Integer.parseInt(m.group(2));
            // replace the current number in the string with the number +1
            return m.group(1) + (number + 1);
        } else {
            return className + "__1";
        }
    }
}
