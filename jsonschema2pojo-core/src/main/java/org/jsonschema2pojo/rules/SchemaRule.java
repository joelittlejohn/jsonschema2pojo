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

import static org.apache.commons.lang3.StringUtils.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Applies a JSON schema.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5</a>
 */
public class SchemaRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected SchemaRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * At the root of a schema document this rule should be applied (schema
     * documents contain a schema), but also in many places within the document.
     * Each property of type "object" is itself defined by a schema, the items
     * attribute of an array is a schema, the additionalProperties attribute of
     * a schema is also a schema.
     * <p>
     * Where the schema value is a $ref, the ref URI is assumed to be applicable
     * as a URL (from which content will be read). Where the ref URI has been
     * encountered before, the root Java type created by that schema will be
     * re-used (generation steps won't be repeated).
     * 
     * @param schema
     *            the schema within which this schema rule is being applied
     */
    @Override
    public JType apply(String nodeName, JsonNode schemaNode, JsonNode parent, JClassContainer generatableType, Schema schema) {

        if (schemaNode.has("$ref")) {
            final String nameFromRef = nameFromRef(schemaNode.get("$ref").asText());

            schema = ruleFactory.getSchemaStore().create(schema, schemaNode.get("$ref").asText(), ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
            schemaNode = schema.getContent();

            if (schema.isGenerated()) {
                return schema.getJavaType();
            }

            return apply(nameFromRef != null ? nameFromRef : nodeName, schemaNode, parent, generatableType, schema);
        }

        schema = schema.deriveChildSchema(schemaNode);

        JType javaType;
        if (schemaNode.has("enum")) {
            javaType = ruleFactory.getEnumRule().apply(nodeName, schemaNode, parent, generatableType, schema);
        } else {
            javaType = ruleFactory.getTypeRule().apply(nodeName, schemaNode, parent, generatableType.getPackage(), schema);
        }
        schema.setJavaTypeIfEmpty(javaType);

        return javaType;
    }
    
    private String nameFromRef(String ref) {
        
        if ("#".equals(ref)) {
            return null;
        }
        
        String nameFromRef;
        if (!contains(ref, "#")) {
            nameFromRef = Jsonschema2Pojo.getNodeName(ref, ruleFactory.getGenerationConfig());
        } else {
            String[] nameParts = split(ref, "/\\#");
            nameFromRef = nameParts[nameParts.length - 1];
        }
        
        try {
            return URLDecoder.decode(nameFromRef, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new GenerationException("Failed to decode ref: " + ref, e);
        }
    }
}
