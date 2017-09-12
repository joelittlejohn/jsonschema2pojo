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

object VarToVal extends VarToVal

object defs {
  type Vars = List[Map[String,VariableDeclaration]]
}

/**
 * VarToVal changes var to val if no reassignments are done 
 */
class VarToVal extends ModifierVisitor[Vars] with UnitTransformer {
  
  private val operators = Set(Unary.posDecrement, Unary.posIncrement,
      Unary.preDecrement, Unary.preIncrement)
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, Nil).asInstanceOf[CompilationUnit] 
  }  
  
  override def visit(n: Block, arg: Vars): Node = {      
    if (n.getStmts == null) {
      return n
    }
    val vars = n.getStmts.collect { case Stmt(v: VariableDeclaration) => v }
      .flatMap(v => v.getVars.map(va => (va.getId.getName,v)))
      .toMap    
    // set vars to final
    vars.values.foreach(_.addModifier(ModifierSet.FINAL))    
    super.visit(n,  vars :: arg)
  }
  
  override def visit(n: Assign, arg: Vars): Node = {
    removeFinal(n.getTarget.toString, arg)
    n
  }
  
  override def visit(n: Unary, arg: Vars): Node = {
    if (operators.contains(n.getOperator)) {
      removeFinal(n.getExpr.toString, arg) 
    } 
    n
  }
  
  // leave VariableDeclarations unchanged
  override def visit(n: VariableDeclaration, arg: Vars): Node = n
    
  private def removeFinal(key: String, arg: Vars) {
    arg.find(_.contains(key))        
      .flatMap(_.get(key))
      .foreach(_.removeModifier(ModifierSet.FINAL))    
  }
  
}