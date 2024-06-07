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

import java.util.Objects;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.ReflectionHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;

public class BuilderRule implements Rule<JDefinedClass, JDefinedClass> {

  private RuleFactory ruleFactory;
  private ReflectionHelper reflectionHelper;

  BuilderRule(RuleFactory ruleFactory, ReflectionHelper reflectionHelper) {
    this.ruleFactory = ruleFactory;
    this.reflectionHelper = reflectionHelper;
  }

  @Override
  public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass instanceClass, Schema currentSchema) {

    // Create the inner class for the builder
    JDefinedClass concreteBuilderClass;
    JDefinedClass builderClass;

    try {

      String concreteBuilderClassName = ruleFactory.getNameHelper().getBuilderClassName(instanceClass);
      String builderClassName = ruleFactory.getNameHelper().getBaseBuilderClassName(instanceClass);

      builderClass = instanceClass._class(JMod.ABSTRACT + JMod.PUBLIC + JMod.STATIC, builderClassName);

      concreteBuilderClass = instanceClass._class(JMod.PUBLIC + JMod.STATIC, concreteBuilderClassName);
      concreteBuilderClass._extends(builderClass.narrow(instanceClass));

    } catch (JClassAlreadyExistsException e) {
      return e.getExistingClass();
    }

    // Determine which base builder (if any) this builder should inherit from
    JClass parentBuilderClass = null;
    JClass parentClass = instanceClass._extends();
    if (!(parentClass.isPrimitive() || reflectionHelper.isFinal(parentClass) || Objects.equals(parentClass.fullName(), "java.lang.Object"))) {
      parentBuilderClass = reflectionHelper.getBaseBuilderClass(parentClass);
    }

    // Determine the generic type name and that the builder will create instances of
    String builderTypeParameterName = ruleFactory.getNameHelper().getBuilderTypeParameterName(instanceClass);
    JTypeVar instanceType = builderClass.generify(builderTypeParameterName, instanceClass);

    // For new builders we need to create an instance variable and 'build' method
    // for inheriting builders we'll receive these from the superType
    if (parentBuilderClass == null) {

      // Create the instance variable
      JFieldVar instanceField = builderClass.field(JMod.PROTECTED, instanceType, "instance");

      // Create the actual "build" method
      JMethod buildMethod = builderClass.method(JMod.PUBLIC, instanceType, "build");

      JBlock body = buildMethod.body();
      JVar result = body.decl(instanceType, "result");
      body.assign(result, JExpr._this().ref(instanceField));
      body.assign(JExpr._this().ref(instanceField), JExpr._null());
      body._return(result);

      // Create the noargs builder constructors
      generateNoArgsBuilderConstructors(instanceClass, builderClass, concreteBuilderClass);
    } else {
      // Declare the inheritance
      builderClass._extends(parentBuilderClass.narrow(parentBuilderClass.owner().ref(builderTypeParameterName)));

      JMethod buildMethod = builderClass.method(JMod.PUBLIC, instanceType, "build");
      buildMethod.annotate(Override.class);

      JBlock body = buildMethod.body();
      body._return(JExpr._super().invoke("build"));

      // Create the noargs builder constructors
      generateNoArgsBuilderConstructors(instanceClass, builderClass, concreteBuilderClass);
    }

    JMethod builderMethod = instanceClass.method(JMod.PUBLIC + JMod.STATIC, builderClass.narrow(instanceClass.wildcard()), "builder");
    JBlock builderBody = builderMethod.body();
    builderBody._return(JExpr._new(concreteBuilderClass));

    return builderClass;
  }

  private void generateNoArgsBuilderConstructors(JDefinedClass instanceClass, JDefinedClass baseBuilderClass, JDefinedClass builderClass) {

    generateNoArgsBaseBuilderConstructor(instanceClass, baseBuilderClass, builderClass);
    generateNoArgsBuilderConstructor(instanceClass, baseBuilderClass, builderClass);
  }

  private void generateNoArgsBuilderConstructor(JDefinedClass instanceClass, JDefinedClass baseBuilderClass, JDefinedClass builderClass) {

    // Add the constructor to builder.
    JMethod noArgsConstructor = builderClass.constructor(JMod.PUBLIC);
    JBlock constructorBlock = noArgsConstructor.body();

    // Determine if we need to invoke the super() method for our parent builder
    if (!(baseBuilderClass.isPrimitive() || reflectionHelper.isFinal(baseBuilderClass) || Objects.equals(baseBuilderClass.fullName(), "java.lang.Object"))) {
      constructorBlock.invoke("super");
    }
  }

  private void generateNoArgsBaseBuilderConstructor(JDefinedClass instanceClass, JDefinedClass builderClass, JDefinedClass concreteBuilderClass) {

    JMethod noArgsConstructor = builderClass.constructor(JMod.PUBLIC);
    JAnnotationUse warningSuppression = noArgsConstructor.annotate(SuppressWarnings.class);
    warningSuppression.param("value", "unchecked");

    JBlock constructorBlock = noArgsConstructor.body();

    JFieldVar instanceField = reflectionHelper.searchClassAndSuperClassesForField("instance", builderClass);

    // Determine if we need to invoke the super() method for our parent builder
    JClass parentClass = builderClass._extends();
    if (!(parentClass.isPrimitive() || reflectionHelper.isFinal(parentClass) || Objects.equals(parentClass.fullName(), "java.lang.Object"))) {
      constructorBlock.invoke("super");
    }

    // Only initialize the instance if the object being constructed is actually this class
    // if it's a subtype then ignore the instance initialization since the subclass will initialize it
    constructorBlock.directStatement("// Skip initialization when called from subclass");
    JInvocation comparison = JExpr._this().invoke("getClass").invoke("equals").arg(JExpr.dotclass(concreteBuilderClass));
    JConditional ifNotSubclass = constructorBlock._if(comparison);
    ifNotSubclass._then().assign(JExpr._this().ref(instanceField), JExpr.cast(instanceField.type(), JExpr._new(instanceClass)));
  }
}
