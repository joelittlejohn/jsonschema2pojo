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

import java.util.ArrayList
import japa.parser.ast.CompilationUnit
import UnitTransformer._

object SerialVersionUID extends SerialVersionUID

/**
 * SerialVersionUID turns serialVersionUID fields into annotations
 */
class SerialVersionUID extends UnitTransformerBase {
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(nn: ClassOrInterfaceDecl, cu: CompilationUnit): ClassOrInterfaceDecl = {      
    val n = super.visit(nn, cu).asInstanceOf[ClassOrInterfaceDecl]
    if (n.getMembers == null) {
      return n
    }
    
    val varAndField = n.getMembers.collect { case f: Field => f }    
       .flatMap { f => f.getVariables.map( v => (v.getId.getName,v,f)) }
       .find(_._1 == "serialVersionUID").map(t => (t._2,t._3))
       .getOrElse(null)
       
    if (varAndField != null) {
      //varAndField._2.getVariables.remove(varAndField._1)
      varAndField._2.setVariables(varAndField._2.getVariables.filterNot(_ == varAndField._1))
      if (varAndField._2.getVariables.isEmpty) {
        //n.getMembers.remove(varAndField._2)
        n.setMembers( n.getMembers.filterNot(_ == varAndField._2) )
      }
      val value = varAndField._1.getInit
      n.setAnnotations(new SingleMemberAnnotation("SerialVersionUID", value) :: n.getAnnotations)
    }
    n
  }
  
}