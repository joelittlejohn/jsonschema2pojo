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

object Properties extends Properties

/**
 * Properties turns field + accessor combinations into annotated 
 * Scala properties
 */
class Properties extends UnitTransformerBase {
    
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(n: ClassOrInterfaceDecl, cu: CompilationUnit): ClassOrInterfaceDecl = {      
    val t = super.visit(n, cu).asInstanceOf[ClassOrInterfaceDecl]
    
    // accessors
    val getters = t.getMembers.collect { case m: Method => m }
      .filter(m => isGetter(m))
      .map(m => (m.getName,m)).toMap      
    
    // fields with accessors
    val fields = t.getMembers.collect { case f: Field => f }
      .filter(_.getModifiers.isPrivate)
      .flatMap( f => f.getVariables.map( v => (v.getId.getName,v,f) ))
      .filter { case (name,_,_) =>  getters.contains(name) }
          
    // remove accessors 
    for ( (name, variable, field) <- fields) {
      var getter = getters(name)
      val body = getter.getBody
      if (getter.getModifiers.isAbstract) {
        t.setMembers(t.getMembers.filterNot(_ == getter))
        field.removeModifier(PRIVATE)
      } else if (isReturnFieldStmt(body(0))) {
        //t.getMembers.remove(getter)
        t.setMembers(t.getMembers.filterNot(_ == getter))
        field.setModifiers(getter.getModifiers)
      } else if (isLazyCreation(body,name)) {
        //t.getMembers.remove(getter)
        t.setMembers(t.getMembers.filterNot(_ == getter))
        variable.setInit(getLazyInit(body))
        field.setModifiers(getter.getModifiers
          .addModifier(LAZY).addModifier(ModifierSet.FINAL))
      }            
    }    
    t
  }
  
  private def isGetter(method: Method): Boolean = method match {
    case Method(n, t, Nil, Block(_ :: rest)) if !t.isInstanceOf[VoidType] => true
    case _ => false
  }    
      
}
