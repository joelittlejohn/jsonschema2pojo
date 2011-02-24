/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo;

import org.codehaus.jackson.JsonNode;

/**
 * Resolves fragments of a schema.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-6.2">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-6.2</a>
 */
public interface FragmentResolver {

    /**
     * Resolves a fragment path and returns a fragment of the given tree.
     * 
     * @param tree
     *            the complete tree of nodes, against which the path will be
     *            resolved.
     * @param path
     *            the path that represents a fragment within the document
     *            (either slash-delimited or dot-delimited).
     * @return a part/fragment of the document, referred to by the given path
     */
    JsonNode resolve(JsonNode tree, String path);

}
