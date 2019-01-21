/**
 * Copyright Â© 2010-2017 Nokia
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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ReflectionHelper;

public class ConstructorRule  implements Rule<JPackage, JType> {

  private final RuleFactory ruleFactory;
  private final ReflectionHelper reflectionHelper;

  public ConstructorRule(RuleFactory ruleFactory, ReflectionHelper reflectionHelper) {
    this.ruleFactory = ruleFactory;
    this.reflectionHelper = reflectionHelper;
  }

  /**
   * Retrieve the list of properties to go in the constructor from node. This
   * is all properties listed in node["properties"] if ! onlyRequired, and
   * only required properties if onlyRequired.
   *
   * @param node
   * @return
   */
  private LinkedHashSet<String> getConstructorProperties(JsonNode node, boolean onlyRequired) {

    if (!node.has("properties")) {
      return new LinkedHashSet<>();
    }

    LinkedHashSet<String> rtn = new LinkedHashSet<>();
    Set<String> draft4RequiredProperties = new HashSet<>();

    // setup the set of required properties for draft4 style "required"
    if (onlyRequired && node.has("required")) {
      JsonNode requiredArray =  node.get("required");
      if (requiredArray.isArray()) {
        for (JsonNode requiredEntry: requiredArray) {
          if (requiredEntry.isTextual()) {
            draft4RequiredProperties.add(requiredEntry.asText());
          }
        }
      }
    }

    NameHelper nameHelper = ruleFactory.getNameHelper();
    for (Iterator<Entry<String, JsonNode>> properties = node.get("properties").fields(); properties.hasNext();) {
      Map.Entry<String, JsonNode> property = properties.next();

      JsonNode propertyObj = property.getValue();
      if (onlyRequired) {
        // draft3 style
        if (propertyObj.has("required") && propertyObj.get("required").asBoolean()) {
          rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
        }

        // draft4 style
        if (draft4RequiredProperties.contains(property.getKey())) {
          rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
        }
      } else {
        rtn.add(nameHelper.getPropertyName(property.getKey(), property.getValue()));
      }
    }
    return rtn;
  }

  /**
   * Recursive, walks the schema tree and assembles a list of all properties of this schema's super schemas
   */
  private LinkedHashSet<String> getSuperTypeConstructorPropertiesRecursive(JsonNode node, Schema schema, boolean onlyRequired) {
    Schema superTypeSchema = reflectionHelper.getSuperSchema(node, schema, true);

    if (superTypeSchema == null) {
      return new LinkedHashSet<>();
    }

    JsonNode superSchemaNode = superTypeSchema.getContent();

    LinkedHashSet<String> rtn = getConstructorProperties(superSchemaNode, onlyRequired);
    rtn.addAll(getSuperTypeConstructorPropertiesRecursive(superSchemaNode, superTypeSchema, onlyRequired));

    return rtn;
  }

  @Override
  public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage _package, Schema currentSchema) {
    GenerationConfig generationConfig = ruleFactory.getGenerationConfig();

    JDefinedClass jclass = reflectionHelper._getClass(nodeName, node, _package);

    LinkedHashSet<String> classProperties = getConstructorProperties(node, generationConfig.isConstructorsRequiredPropertiesOnly());
    LinkedHashSet<String> combinedSuperProperties = getSuperTypeConstructorPropertiesRecursive(node, currentSchema, generationConfig.isConstructorsRequiredPropertiesOnly());

    // no properties to put in the constructor => default constructor is good enough.
    if (classProperties.isEmpty() && combinedSuperProperties.isEmpty()) {
      return jclass;
    }

    // add a no-args constructor for serialization purposes
    JMethod noargsConstructor = jclass.constructor(JMod.PUBLIC);
    noargsConstructor.javadoc().add("No args constructor for use in serialization");

    // add the public constructor with property parameters
    JMethod fieldsConstructor = jclass.constructor(JMod.PUBLIC);
    JBlock constructorBody = fieldsConstructor.body();
    JInvocation superInvocation = constructorBody.invoke("super");

    Map<String, JFieldVar> fields = jclass.fields();
    Map<String, JVar> classFieldParams = new HashMap<>();

    for (String property : classProperties) {
      JFieldVar field = fields.get(property);

      if (field == null) {
        throw new IllegalStateException("Property " + property + " hasn't been added to JDefinedClass before calling addConstructors");
      }

      fieldsConstructor.javadoc().addParam(property);
      JVar param = fieldsConstructor.param(field.type(), field.name());
      constructorBody.assign(JExpr._this().ref(field), param);
      classFieldParams.put(property, param);
    }

    List<JVar> superConstructorParams = new ArrayList<>();


    for (String property : combinedSuperProperties) {
      JFieldVar field = reflectionHelper.searchSuperClassesForField(property, jclass);

      if (field == null) {
        throw new IllegalStateException("Property " + property + " hasn't been added to JDefinedClass before calling addConstructors");
      }

      JVar param = classFieldParams.get(property);

      if (param == null) {
        param = fieldsConstructor.param(field.type(), field.name());
      }

      fieldsConstructor.javadoc().addParam(property);
      superConstructorParams.add(param);
    }

    for (JVar param : superConstructorParams) {
      superInvocation.arg(param);
    }

    return jclass;
  }
}
