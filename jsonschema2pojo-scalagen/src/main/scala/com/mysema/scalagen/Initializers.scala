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
import UnitTransformer._

object Initializers extends Initializers

/**
 * Initializers normalizes initializer blocks
 */
class Initializers extends UnitTransformerBase {
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(ci: ClassOrInterfaceDecl, cu: CompilationUnit): Node = {
    val t = super.visit(ci, cu).asInstanceOf[ClassOrInterfaceDecl]
    if (t.getMembers == null) {
      return t
    }
    
    val initializers = t.getMembers.collect { case i: Initializer => i }
    if (!initializers.isEmpty) {
      val fields = t.getMembers.collect { case f: Field => f }
      val variables = fields.flatMap(_.getVariables).map(v => (v.getId.getName, v)).toMap
      
      for (i <- initializers) {
        i.getBlock.setStmts(i.getBlock.getStmts.filter(_ match {
          case Stmt((t: Name) set v) if variables.contains(t.getName) => {
            variables(t.getName).setInit(v)
            false
          }
          case _ => true
        }))
      }
      
      // remove empty initializers
      val emptyInitializerBlocks = initializers.filter(_.getBlock.isEmpty)
      t.setMembers( t.getMembers.filterNot(emptyInitializerBlocks.contains) )      
    }
    t
  }
  
}