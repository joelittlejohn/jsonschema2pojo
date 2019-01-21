/**
 * Copyright Â© 2010-2017 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.ReflectionHelper;

public class BuilderRule implements Rule<JDefinedClass, JDefinedClass> {

  private RuleFactory ruleFactory;
  private ReflectionHelper reflectionHelper;

  BuilderRule(RuleFactory ruleFactory, ReflectionHelper reflectionHelper) {
    this.ruleFactory = ruleFactory;
    this.reflectionHelper = reflectionHelper;
  }

  @Override
  public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass generatableType, Schema currentSchema) {

    JClass parentBuilderClass = null;
    JClass parentClass = generatableType._extends();
    if (!(parentClass.isPrimitive() || reflectionHelper.isFinal(parentClass) || Objects.equals(parentClass.fullName(), "java.lang.Object"))) {
      String superTypeName = ruleFactory.getNameHelper().getBuilderClassName(parentClass);
      parentBuilderClass = reflectionHelper._getClass(superTypeName, parentClass._package());
    }

    // Create the inner class for the builder
    JDefinedClass builderClass = null;
    JTypeVar instanceType = null;
    try {
      String builderName = ruleFactory.getNameHelper().getBuilderClassName(generatableType);
      builderClass = generatableType._class(JMod.PUBLIC + JMod.STATIC, builderName);
      instanceType = builderClass.generify("T", generatableType);

      if (parentBuilderClass != null) {
        builderClass._extends(parentBuilderClass);
      }
    } catch (JClassAlreadyExistsException e) {
      return e.getExistingClass();
    }

    // Create the instance variable
    JFieldVar instanceField = builderClass.field(JMod.PRIVATE, instanceType, "instance");

    // Create the actual "build" method
    JMethod buildMethod = builderClass.method(JMod.PUBLIC, instanceType, "build");

    JBlock body = buildMethod.body();
    JVar result = body.decl(instanceType, "result");
    body.assign(result, JExpr._this().

        ref(instanceField));
    body.assign(JExpr._this().

        ref(instanceField), JExpr.

        _null());
    body._return(result);

    return builderClass;
  }

}
