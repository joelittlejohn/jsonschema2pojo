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
import java.util.Objects;
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
  public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass instanceClass, Schema currentSchema) {

    // Create the inner class for the builder
    JDefinedClass builderClass;

    try {
      String builderName = ruleFactory.getNameHelper().getBuilderClassName(instanceClass);
      builderClass = instanceClass._class(JMod.PUBLIC + JMod.STATIC, builderName);
    } catch (JClassAlreadyExistsException e) {
      return e.getExistingClass();
    }

    // Determine which builder (if any) this builder should inherit from
    JClass parentBuilderClass = null;
    JClass parentClass = instanceClass._extends();
    if (!(parentClass.isPrimitive() || reflectionHelper.isFinal(parentClass) || Objects.equals(parentClass.fullName(), "java.lang.Object"))) {
      parentBuilderClass = reflectionHelper.getBuilderClass(parentClass);
    }

    // Determine the generic type 'T' that the builder will create instances of
    JTypeVar instanceType = builderClass.generify("T", instanceClass);

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

      // Create the noargs builder constructor
      generateNoArgsBuilderConstructor(instanceClass, builderClass);
    } else {
      // Declare the inheritance
      builderClass._extends(parentBuilderClass);
      
      JMethod buildMethod = builderClass.method(JMod.PUBLIC, instanceType, "build");
      buildMethod.annotate(Override.class);

      JBlock body = buildMethod.body();
      body._return(JExpr.cast(instanceType, JExpr._super().invoke("build")));

      // Create the noargs builder constructor
      generateNoArgsBuilderConstructor(instanceClass, builderClass);
    }

    return builderClass;
  }

  private void generateNoArgsBuilderConstructor(JDefinedClass instanceClass, JDefinedClass builderClass) {
    JMethod noargsConstructor = builderClass.constructor(JMod.PUBLIC);
    JAnnotationUse warningSuppression = noargsConstructor.annotate(SuppressWarnings.class);
    warningSuppression.param("value", "unchecked");

    JBlock constructorBlock = noargsConstructor.body();

    JFieldVar instanceField = reflectionHelper.searchClassAndSuperClassesForField("instance", builderClass);

    // Determine if we need to invoke the super() method for our parent builder
    JClass parentClass = builderClass._extends();
    if (!(parentClass.isPrimitive() || reflectionHelper.isFinal(parentClass) || Objects.equals(parentClass.fullName(), "java.lang.Object"))) {
      constructorBlock.invoke("super");
    }

    // Only initialize the instance if the object being constructed is actually this class
    // if it's a subtype then ignore the instance initialization since the subclass will initialize it
    constructorBlock.directStatement("// Skip initialization when called from subclass");
    JInvocation comparison = JExpr._this().invoke("getClass").invoke("equals").arg(JExpr.dotclass(builderClass));
    JConditional ifNotSubclass = constructorBlock._if(comparison);
    ifNotSubclass._then().assign(JExpr._this().ref(instanceField), JExpr.cast(instanceField.type(), JExpr._new(instanceClass)));
  }

}
