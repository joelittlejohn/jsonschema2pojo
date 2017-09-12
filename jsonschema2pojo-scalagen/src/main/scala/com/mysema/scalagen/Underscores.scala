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

object Underscores extends Underscores

/**
 * Underscores strips off underscore prefixes from field names with related Bean properties
 */
class Underscores extends UnitTransformerBase with BeanHelpers {
    
  private val nameReplacer = new ModifierVisitor[Set[String]] {
    
    override def visitName(n: String, arg: Set[String]): String = {
      if (arg.contains(n)) n.substring(1) else n
    }  
  }
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(n: ClassOrInterfaceDecl, cu: CompilationUnit): Node = {
    val getters = n.getMembers.collect { case m: Method => m }
      .filter(m => isBeanGetter(m) || isBooleanBeanGetter(m))
      .map(getProperty)      
    
    val variables = n.getMembers.collect { case f: Field => f }
      .flatMap( _.getVariables)
      .map(_.getId.getName)
      .filter(n => n.startsWith("_") && getters.contains(n.substring(1)))
      .toSet
      
    n.accept(nameReplacer, variables)
  }
  
}