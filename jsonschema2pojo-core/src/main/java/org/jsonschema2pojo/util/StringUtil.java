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

import com.sun.codemodel.JType;

/**
 * Created by sun on 6/5/15.
 */
public class StringUtil {
    public static String getGenericType(JType jType) {
        if (jType.erasure().name().equals("List")) {
            final String typeName = jType.fullName();
            int start = 0;
            int end = typeName.length();

            for (int i = 0; i < typeName.length(); ++i) {
                switch (typeName.charAt(i)) {
                    case '<':
                        start = i;
                        break;
                    case '>':
                        end = i;
                        break;
                }
            }
            // plus one for excluding '<'
            return typeName.substring(start+1, end);
        }
        return jType.erasure().name();
    }
}
