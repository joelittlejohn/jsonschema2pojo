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

object Rethrows extends Rethrows

/**
 * Rethrows unwraps try/catch blocks with simple rethrows
 */
class Rethrows extends UnitTransformerBase {
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
    
  override def visit(n: Try, arg: CompilationUnit): Node = {
    if (n.getFinallyBlock == null && !isEmpty(n.getCatchs) && n.getCatchs.filter(isPrinted).isEmpty) {
      extract(super.visit(n.getTryBlock, arg).asInstanceOf[Statement])
    } else {
      super.visit(n, arg)
    }
  }
  
  private def isPrinted(c: Catch): Boolean = {
    val block = c.getCatchBlock()
    block.isEmpty || block.size > 1 || (block.size == 1 && !block(0).isInstanceOf[Throw])
  }
    
}