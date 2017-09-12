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
import defs._
import japa.parser.ast.body.ModifierSet

object Synchronized extends Synchronized

/**
 * 
 */
class Synchronized extends ModifierVisitor[CompilationUnit] with UnitTransformer {
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(nn: Method, arg: CompilationUnit) = {
    val n = super.visit(nn, arg).asInstanceOf[Method]
    if (n.getModifiers.hasModifier(ModifierSet.SYNCHRONIZED)) {
      n.removeModifier(ModifierSet.SYNCHRONIZED)
      n.setBody(new SynchronizedStmt(null, n.getBody()))
    }
    n
  }
  
}  