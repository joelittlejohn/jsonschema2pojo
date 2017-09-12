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

object RemoveAsserts extends RemoveAsserts

/**
 * RemoveAsserts unwraps assertion method call
 */
class RemoveAsserts extends UnitTransformerBase {
  
  private val methods = Set("hasLength","hasText","notEmpty","notNull") 
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
    
  // TODO : don't remove method calls when used as statements
  
  override def visit(n: MethodCall, arg: CompilationUnit) = n match {
    case MethodCall(str("Assert"), _, a :: rest) => a.accept(this, arg)
    case _ => super.visit(n, arg)
  }
    
}