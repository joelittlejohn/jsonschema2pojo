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

import static java.util.Arrays.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class FragmentResolver {

    public JsonNode resolve(JsonNode tree, String path, String refFragmentPathDelimiters) {

        return resolve(tree, new ArrayList<>(asList(split(path, refFragmentPathDelimiters))));

    }

    private JsonNode resolve(JsonNode tree, List<String> path) {

        if (path.isEmpty()) {
            return tree;
        } else {
            String part = path.remove(0);

            if (tree.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    return resolve(tree.get(index), path);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a valid array index: " + part);
                }
            }

            if (tree.has(part)) {
                return resolve(tree.get(part), path);
            } else {
                throw new IllegalArgumentException("Path not present: " + part);
            }
        }

    }
}
