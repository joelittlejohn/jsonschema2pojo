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

import org.apache.commons.lang3.StringUtils;

public class MakeUniqueClassName {

    /**
     * When the class name is not unique we will use two underscore '__' and a digit representing the number of time
     * this class was found
     */
    public static String makeUnique(String className) {
        String returnClassName = className;
        // Last character is a digit and there is 2 underscore in the className
        if (Character.isDigit(className.charAt(className.length() - 1)) && StringUtils.contains(className, "__")) {
            // get the number
            String strNumber = className.substring(StringUtils.indexOf(className, "__") + 2);
            Integer number = Integer.parseInt(strNumber);
            // replace the number in the string with +1
            number = number + 1;
            returnClassName = returnClassName.substring(0, StringUtils.indexOf(returnClassName, "__") + 2) + number;
        } else {
            returnClassName = returnClassName + "__1";
        }
        return returnClassName;
    }
}
