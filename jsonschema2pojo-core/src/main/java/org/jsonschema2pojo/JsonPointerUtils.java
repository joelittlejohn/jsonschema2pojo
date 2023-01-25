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

package org.jsonschema2pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;

public class JsonPointerUtils {

    private static Map<String,String> SUBSTITUTIONS = new LinkedHashMap<String, String>() {{
        put("~", "~0");
        put("/", "~1");
        put("#", "~2");
        put(".", "~3");
    }};

    public static String encodeReferenceToken(final String s) {
        String encoded = s;
        for (Map.Entry<String,String> sub : SUBSTITUTIONS.entrySet()) {
            encoded = encoded.replace(sub.getKey(), sub.getValue());
        }
        return encoded;
    }
    
    public static String decodeReferenceToken(final String s) {
        String decoded = s;

        List<String> reverseOrderedKeys = new ArrayList<String>(SUBSTITUTIONS.keySet());
        Collections.reverse(reverseOrderedKeys);
        for (String key : reverseOrderedKeys) {
            decoded = decoded.replace(SUBSTITUTIONS.get(key), key);
        }

        return decoded;
    }
}
