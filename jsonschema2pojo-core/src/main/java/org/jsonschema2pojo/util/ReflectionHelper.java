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

package org.jsonschema2pojo.util;

import static org.jsonschema2pojo.util.TypeUtil.resolveType;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.RuleFactory;

public class ReflectionHelper {

  private RuleFactory ruleFactory;

  public ReflectionHelper(RuleFactory ruleFactory) {
    this.ruleFactory = ruleFactory;
  }

  public JType getSuperType(String nodeName, JsonNode node, JPackage jPackage, Schema schema) {
    if (node.has("extends") && node.has("extendsJavaClass")) {
      throw new IllegalStateException("'extends' and 'extendsJavaClass' defined simultaneously");
    }

    JType superType = jPackage.owner().ref(Object.class);
    Schema superTypeSchema = getSuperSchema(node, schema, false);
    if (superTypeSchema != null) {
      superType = ruleFactory.getSchemaRule().apply(nodeName + "Parent", node.get("extends"), node, jPackage, superTypeSchema);
    } else if (node.has("extendsJavaClass")) {
      superType = resolveType(jPackage, node.get("extendsJavaClass").asText());
    }

    return superType;
  }

  public Schema getSuperSchema(JsonNode node, Schema schema, boolean followRefs) {
    if (node.has("extends")) {
      String path;
      if (schema.getId().getFragment() == null) {
        path = "#extends";
      } else {
        path = "#" + schema.getId().getFragment() + "/extends";
      }

      Schema superSchema = ruleFactory.getSchemaStore().create(schema, path, ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());

      if (followRefs) {
        superSchema = resolveSchemaRefsRecursive(superSchema);
      }

      return superSchema;
    }
    return null;
  }

  /**
   * This is recursive with searchClassAndSuperClassesForField
   */
  public JFieldVar searchSuperClassesForField(String property, JDefinedClass jclass) {
    JClass superClass = jclass._extends();
    JDefinedClass definedSuperClass = definedClassOrNullFromType(superClass);
    if (definedSuperClass == null) {
      return null;
    }
    return searchClassAndSuperClassesForField(property, definedSuperClass);
  }

  public JDefinedClass getBuilderClass(JDefinedClass target) {
    String builderClassname = ruleFactory.getNameHelper().getBuilderClassName(target);

    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(target.classes(), Spliterator.ORDERED), false)
        .filter(definedClass -> definedClass.name().equals(builderClassname)).findFirst().orElse(null);
  }

  public JDefinedClass getBuilderClass(JClass target) {
    String builderClassname = ruleFactory.getNameHelper().getBuilderClassName(target);
    return getAllPackageClasses(target._package()).stream().filter(definedClass -> definedClass.name().equals(builderClassname)).findFirst()
        .orElse(null);
  }

  public boolean isFinal(JType superType) {
    try {
      Class<?> javaClass = Class.forName(superType.fullName());
      return Modifier.isFinal(javaClass.getModifiers());
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public JFieldVar searchClassAndSuperClassesForField(String property, JDefinedClass jclass) {
    Map<String, JFieldVar> fields = jclass.fields();
    JFieldVar field = fields.get(property);
    if (field == null) {
      return searchSuperClassesForField(property, jclass);
    }
    return field;
  }

  private JDefinedClass definedClassOrNullFromType(JType type) {
    if (type == null || type.isPrimitive()) {
      return null;
    }
    JClass fieldClass = type.boxify();
    JPackage jPackage = fieldClass._package();
    return this._getClass(fieldClass.name(), jPackage);
  }

  private JDefinedClass _getClass(String name, JPackage _package) {
    return getAllPackageClasses(_package).stream().filter(definedClass -> definedClass.name().equals(name)).findFirst()
        .orElseThrow(() -> new NoClassDefFoundError(name));
  }

  private Collection<JDefinedClass> getAllPackageClasses(JPackage _package) {
    LinkedList<JDefinedClass> result = new LinkedList<>();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(_package.classes(), Spliterator.ORDERED), false)
        .forEach(_class -> result.addAll(getAllClassClasses(_class)));
    return result;
  }

  private Collection<JDefinedClass> getAllClassClasses(JDefinedClass _class) {
    LinkedList<JDefinedClass> result = new LinkedList<>();
    result.add(_class);

    _class.classes().forEachRemaining(result::add);
    return result;
  }

  private Schema resolveSchemaRefsRecursive(Schema schema) {
    JsonNode schemaNode = schema.getContent();
    if (schemaNode.has("$ref")) {
      schema = ruleFactory.getSchemaStore()
          .create(schema, schemaNode.get("$ref").asText(), ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
      return resolveSchemaRefsRecursive(schema);
    }
    return schema;
  }

}
