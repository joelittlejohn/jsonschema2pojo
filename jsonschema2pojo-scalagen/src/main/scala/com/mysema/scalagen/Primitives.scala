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
import japa.parser.ast.expr._

object Primitives extends Primitives

/**
 * Primitives modifies primitive type related constants and method calls
 */
class Primitives extends UnitTransformerBase {

  private val TRUE = new BooleanLiteral(true)

  private val FALSE = new BooleanLiteral(false)

  private val primitives = Set("Boolean","Byte","Char","Double","Float","Integer","Long","Short")

  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit]
  }

  override def visit(n: FieldAccess, arg: CompilationUnit): Node = n match {
    case FieldAccess(str("Boolean"), "TRUE") => TRUE
    case FieldAccess(str("Boolean"), "FALSE") => FALSE
    case _ => super.visit(n, arg)
  }

//  override def visit(n: MethodCall, arg: CompilationUnit): Node = n match {
//    case MethodCall(str(scope), "valueOf", a :: Nil) => a.accept(this, arg)
//    case _ => super.visit(n, arg)
//  }

}