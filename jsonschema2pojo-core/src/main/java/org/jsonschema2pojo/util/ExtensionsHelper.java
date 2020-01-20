/*
 * Copyright © 2010-2017 Nokia
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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Class for working with json-schema keyword extensions. I.e. processing of any node properties that are not
 * strictly a part of the json-schema specification.
 *
 * This was driven largely out of a need to maintain compatibility with existing schemas defined using keyword
 * extensions that were not prefixed with "x-" before support was added to use the prefix instead.
 */
public class ExtensionsHelper {

    /** Prefix for keyword extensions of the json schema */
    public static final String X_PREFIX = "x-";

    /**
     * Get node.has(property), for property or "x-" + property with the given arguments.
     *
     * @param node  Node on which to retrieve a property.
     * @param property Property to test for existence of it or the prefixed variant.
     * @return Node has one of the property or x-property.
     * @throws IllegalStateException if both 'property' and 'x-property' are found on the node.
     */
    public static boolean hasExtensionProperty(JsonNode node, String property) throws IllegalStateException {
        boolean hasWithoutX = node.has(property);
        boolean hasWithX = node.has(X_PREFIX + property);
        if (hasWithoutX && hasWithX) {
            throw new IllegalStateException("Define only one of '" + property + "' or 'x-" + property + "' (preferred)");
        }
        return hasWithoutX || hasWithX;

    }

    /**
     * Get node.get(property), for property or "x-" + property with the given arguments.
     *
     * @param node  Node on which to retrieve a property.
     * @param property Property to retrieve if exists, and fall back to 'x-' if not.
     * @return Found property.
     */
    public static JsonNode getExtensionProperty(JsonNode node, String property) {
        return node.has(property) ? node.get(property) : node.get(X_PREFIX + property);
    }

    /**
     * Get node.path(property), for property or "x-" + property with the given arguments.
     *
     * @param node  Node on which to retrieve a property.
     * @param property Property to retrieve if exists, and fall back to 'x-' if not.
     * @return Found property.
     */
    public static JsonNode pathExtensionProperty(JsonNode node, String property) {
        return node.has(property) ? node.path(property) : node.path(X_PREFIX + property);
    }

    /**
     * Assuming that the provided node has one of either property or "x-"+property, return the name of which one it
     * actually has. Appropriate behavior is only guaranteed if {@link #hasExtensionProperty(JsonNode, String)} is true
     * for the given property value.
     *
     * @param node Node to check
     * @param property Property for which to discover if it or x-property is used.
     * @return one of property or x-property.
     */
    public static String getExtensionPropertyNameUsed(JsonNode node, String property) {
        return node.has(property) ? property : X_PREFIX + property;
    }

}
