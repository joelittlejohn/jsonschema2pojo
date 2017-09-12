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

import japa.parser.ast.visitor._
import java.util.ArrayList
import UnitTransformer._
import com.mysema.scalagen.ast.BeginClosureExpr

object Setters extends Setters

class Setters extends UnitTransformerBase {
    
  private val thisExpr = new This()
  
  def setterToField(s: String) = {
    val name = s.substring(3)
    name.charAt(0).toLower + name.substring(1)
  }
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }    
  
  override def visit(nn: MethodCall, arg: CompilationUnit): Node = {
    // replaces setter invocations with field access
    val n = super.visit(nn, arg).asInstanceOf[MethodCall]
    if (n.getName.startsWith("set") &&
        (n.getScope == null || n.getScope.isInstanceOf[This]) &&
        n.getArgs.size == 1) {
      val scope = if (n.getScope != null) n.getScope else thisExpr
      new Assign(
          new FieldAccess(scope, setterToField(n.getName)),
          n.getArgs.get(0),
          Assign.assign)      
    } else {
      n
    }
  }
  
    
}