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

import static org.jsonschema2pojo.rules.PrimitiveTypes.*;
import static org.jsonschema2pojo.util.TypeUtil.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

/**
 * Applies the "type" schema rule.
 *
 * @see <a href= "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1">http:/ /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.1</a>
 */
public class TypeRule implements Rule<JClassContainer, JType> {

  private static final String DEFAULT_TYPE_NAME = "any";

  private final RuleFactory ruleFactory;

  protected TypeRule(RuleFactory ruleFactory) {
    this.ruleFactory = ruleFactory;
  }

  /**
   * Applies this schema rule to take the required code generation steps.
   * <p>
   * When applied, this rule reads the details of the given node to determine the appropriate Java type to return. This may be a newly generated type,
   * it may be a primitive type or other type such as {@link java.lang.String} or {@link java.lang.Object}.
   * <p>
   * JSON schema types and their Java type equivalent:
   * <ul>
   * <li>"type":"any" =&gt; {@link java.lang.Object}
   * <li>"type":"array" =&gt; Either {@link java.util.Set} or
   * {@link java.util.List}, see {@link ArrayRule}
   * <li>"type":"boolean" =&gt; <code>boolean</code>
   * <li>"type":"integer" =&gt; <code>int</code>
   * <li>"type":"null" =&gt; {@link java.lang.Object}
   * <li>"type":"number" =&gt; <code>double</code>
   * <li>"type":"object" =&gt; Generated type (see {@link ObjectRule})
   * <li>"type":"string" =&gt; {@link java.lang.String} (or alternative based
   * on presence of "format", see {@link FormatRule})
   * </ul>
   *
   * @param nodeName the name of the node for which this "type" rule applies
   * @param node the node for which this "type" rule applies
   * @param parent the parent node
   * @param jClassContainer the package into which any newly generated type may be placed
   * @return the Java type which, after reading the details of the given schema node, most appropriately matches the "type" specified
   */
  @Override
  public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer jClassContainer, Schema schema) {

    String propertyTypeName = getTypeName(node);

    JType type;

    if (propertyTypeName.equals("object") || node.has("properties") && node.path("properties").size() > 0) {

      type = ruleFactory.getObjectRule().apply(nodeName, node, parent, jClassContainer.getPackage(), schema);
    } else if (node.has("existingJavaType")) {
      String typeName = node.path("existingJavaType").asText();

      if (isPrimitive(typeName, jClassContainer.owner())) {
        type = primitiveType(typeName, jClassContainer.owner());
      } else {
        type = resolveType(jClassContainer, typeName);
      }
    } else if (propertyTypeName.equals("string")) {

      type = jClassContainer.owner().ref(String.class);
    } else if (propertyTypeName.equals("number")) {

      type = getNumberType(jClassContainer.owner(), ruleFactory.getGenerationConfig());
    } else if (propertyTypeName.equals("integer")) {

      type = getIntegerType(jClassContainer.owner(), node, ruleFactory.getGenerationConfig());
    } else if (propertyTypeName.equals("boolean")) {

      type = unboxIfNecessary(jClassContainer.owner().ref(Boolean.class), ruleFactory.getGenerationConfig());
    } else if (propertyTypeName.equals("array")) {

      type = ruleFactory.getArrayRule().apply(nodeName, node, parent, jClassContainer.getPackage(), schema);
    } else {

      type = jClassContainer.owner().ref(Object.class);
    }

    if (!node.has("javaType") && !node.has("existingJavaType") && node.has("format")) {
      type = ruleFactory.getFormatRule().apply(nodeName, node.get("format"), node, type, schema);
    } else if (!node.has("javaType") && !node.has("existingJavaType") && propertyTypeName.equals("string") && node.has("media")) {
      type = ruleFactory.getMediaRule().apply(nodeName, node.get("media"), node, type, schema);
    }

    return type;
  }

  private String getTypeName(JsonNode node) {
    if (node.has("type")) {
      final JsonNode typeNode = node.get("type");
      if (typeNode.isArray() && typeNode.size() > 0) {
        final List<String> typeValues = StreamSupport.stream(typeNode.spliterator(), false)
                .map(JsonNode::asText)
                .filter(n -> !"null".equals(n))
                .collect(Collectors.toList());

        if (typeValues.size() == 1) {
            return typeValues.get(0);
        }
      } else if (typeNode.isTextual()) {
          return typeNode.asText();
      }
    }

    return DEFAULT_TYPE_NAME;
  }

  private JType unboxIfNecessary(JType type, GenerationConfig config) {
    if (config.isUsePrimitives()) {
      return type.unboxify();
    } else {
      return type;
    }
  }

  /**
   * Returns the JType for an integer field. Handles type lookup and unboxing.
   */
  private JType getIntegerType(JCodeModel owner, JsonNode node, GenerationConfig config) {

    if (config.isUseBigIntegers()) {
      return unboxIfNecessary(owner.ref(BigInteger.class), config);
    } else if (config.isUseLongIntegers() || node.has("minimum") && node.get("minimum").isLong() || node.has("maximum") && node.get("maximum")
        .isLong()) {
      return unboxIfNecessary(owner.ref(Long.class), config);
    } else {
      return unboxIfNecessary(owner.ref(Integer.class), config);
    }

  }

  /**
   * Returns the JType for a number field. Handles type lookup and unboxing.
   */
  private JType getNumberType(JCodeModel owner, GenerationConfig config) {

    if (config.isUseBigDecimals()) {
      return unboxIfNecessary(owner.ref(BigDecimal.class), config);
    } else if (config.isUseDoubleNumbers()) {
      return unboxIfNecessary(owner.ref(Double.class), config);
    } else {
      return unboxIfNecessary(owner.ref(Float.class), config);
    }

  }

}
