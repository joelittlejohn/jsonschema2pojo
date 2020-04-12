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

package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

/**
 * <p>
 * Applies the "media" hyper schema rule.
 * </p>
 *
 * @see <a href="http://json-schema.org/latest/json-schema-hypermedia.html#rfc.section.4.3">
 *   Section 4.3 media, JSON Hyper-Schema: Hypertext definitions for JSON Schema</a>
 *
 * @author Christian Trimble
 * @since 0.4.2
 */
public class MediaRule implements Rule<JType, JType> {

    private static final String BINARY_ENCODING = "binaryEncoding";

    /**
     * <p>
     * Constructs a new media rule.
     * </p>
     *
     * @since 0.4.2
     */
    protected MediaRule() {
    }

    /**
     * <p>
     * Applies this schema rule.
     * </p>
     *
     * @param nodeName
     *            the name of the property.
     * @param mediaNode
     *            the media node
     * @param parent
     *            the parent node
     * @param baseType
     *            the type with the media node.  This must be java.lang.String.
     * @param schema
     *            the schema containing the property.
     * @return byte[] when a binary encoding is specified, baseType otherwise.
     * @since 0.4.2
     */
    @Override
    public JType apply(String nodeName, JsonNode mediaNode, JsonNode parent, JType baseType, Schema schema) {
        if (!mediaNode.has(BINARY_ENCODING)) {
            return baseType;
        }

        return baseType.owner().ref(byte[].class);
    }
}
