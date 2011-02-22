/**
 * Copyright © 2010 Nokia
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

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.Schema;
import com.sun.codemodel.JType;

/**
 * Applies the "format" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.23">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.23</a>
 */
public class FormatRule implements SchemaRule<JType, JType> {

    protected FormatRule() {
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * This rule maps format values to Java types:
     * <ul>
     * <li>"format":"date-time" => {@link Date}
     * <li>"format":"date" => {@link String}
     * <li>"format":"time" => {@link String}
     * <li>"format":"utc-millisec" => <code>long</code>
     * <li>"format":"regex" => {@link String}
     * <li>"format":"color" => {@link String}
     * <li>"format":"style" => {@link String}
     * <li>"format":"phone" => {@link String}
     * <li>"format":"uri" => {@link String}
     * <li>"format":"email" => {@link String}
     * <li>"format":"ip-address" => {@link String}
     * <li>"format":"ipv6" => {@link String}
     * <li>"format":"host-name" => {@link String}
     * <li>other (unrecognised format) => baseType
     * </ul>
     * 
     * @param nodeName
     *            the name of the node to which this format is applied
     * @param node
     *            the format node
     * @param baseType
     *            the type which which is being formatted e.g. for
     *            <code>{ "type" : "string", "format" : "uri" }</code> the
     *            baseType would be java.lang.String
     * @throws IllegalArgumentException
     *             when the format value is not recognised
     * @return the Java type that is appropriate for the format value
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JType baseType, Schema schema) {

        if (node.getTextValue().equals("date-time")) {
            return baseType.owner().ref(Date.class);

        } else if (node.getTextValue().equals("date")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("time")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("utc-millisec")) {
            return baseType.owner().LONG;

        } else if (node.getTextValue().equals("regex")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("color")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("style")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("phone")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("uri")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("email")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("ip-address")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("ipv6")) {
            return baseType.owner().ref(String.class);

        } else if (node.getTextValue().equals("host-name")) {
            return baseType.owner().ref(String.class);

        } else {
            return baseType;
        }

    }

}
