/*
 * Copyright (C) 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.scalagen

import japa.parser.ast.body.ModifierSet
import java.util.ArrayList
import com.mysema.scala.BeanUtils
import UnitTransformer._

/**
 * BeanProperties turns field + accessor combinations into @BeanProperty annotated 
 * Scala properties
 */
class BeanProperties(targetVersion: ScalaVersion) extends UnitTransformerBase with BeanHelpers {
  
  val BEAN_PROPERTY_IMPORT =
    if (targetVersion >= Scala210) new Import("scala.beans.{BeanProperty, BooleanBeanProperty}", false, false)
    else new Import("scala.reflect.{BeanProperty, BooleanBeanProperty}", false, false)
     
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(n: ClassOrInterfaceDecl, cu: CompilationUnit): ClassOrInterfaceDecl = {      
    // merges getters and setters into properties
    val t = super.visit(n, cu).asInstanceOf[ClassOrInterfaceDecl]
    
    // accessors
    val methods = t.getMembers.collect { case m: Method => m }
    val getters = methods.filter(m => isBeanGetter(m) || isBooleanBeanGetter(m))
      .map(m => (getProperty(m) ,m)).toMap      
    val setters = methods.filter(m => isBeanSetter(m))
      .map(m => (getProperty(m), m)).toMap
   
    // fields with accessors
    val fields = t.getMembers.collect { case f: Field => f }
      .filter(_.getModifiers.isPrivate)
      .flatMap( f => f.getVariables.map( v => (v.getId.getName,v,f) ))
      .filter { case (name,_,_) =>  getters.contains(name) }
          
    // remove accessors 
    for ( (name, variable, field) <- fields) {
      var getter = getters(name)
      //t.getMembers.remove(getter)
      t.setMembers(t.getMembers.filterNot(_ == getter))
      setters.get(name).foreach { s => t.setMembers(t.getMembers.filterNot(_ == s)) }
      
      // make field public
      val isFinal = field.getModifiers.isFinal
       field.setModifiers(getter.getModifiers
          .addModifier(if (isFinal) ModifierSet.FINAL else 0))
      val annotation = if (getter.getName.startsWith("is")) BOOLEAN_BEAN_PROPERTY else BEAN_PROPERTY 
      if (field.getAnnotations == null || !field.getAnnotations.contains(annotation)) {
        field.setAnnotations(field.getAnnotations :+ annotation)
      }      
      
      // handle lazy init
      if (isLazyCreation(getter.getBody, name)) {
        variable.setInit(getLazyInit(getter.getBody))
        field.addModifier(LAZY)
        if (!setters.contains(name)) {
          field.addModifier(ModifierSet.FINAL)
        }
      }
    }
    
    // add BeanProperty import, if properties have been found
    if (!fields.isEmpty && !cu.getImports.contains(BEAN_PROPERTY_IMPORT)) {
      cu.setImports(cu.getImports :+ BEAN_PROPERTY_IMPORT)
    }
    t
  }
    
}
