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
import japa.parser.ast.visitor._
import java.util.ArrayList
import UnitTransformer._

object Enums extends Enums

/**
 * Enums converts Java enum type declarations into Scala enumerations
 */
class Enums extends UnitTransformerBase {
  
  private val enumerationType = new ClassOrInterface("Enumeration")
  
  private val valType = new ClassOrInterface("Val")
  
  private val valueType = new ClassOrInterface("Value")
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }   
    
  override def visit(n: EnumDecl, arg: CompilationUnit) = {
    // transform enums into Scala Enumerations
    val clazz = new ClassOrInterfaceDecl()
    clazz.setExtends(enumerationType :: Nil)
    clazz.setName(n.getName)
    clazz.setModifiers(OBJECT)
    clazz.setMembers(createMembers(n))
    clazz
  }
  
  private def createMembers(n: EnumDecl): JavaList[BodyDecl] = {
    val typeDecl = new ClassOrInterfaceDecl(0, false, n.getName)
    typeDecl.setExtends(valType :: Nil)
    typeDecl.setImplements(n.getImplements)
    typeDecl.setMembers(n.getMembers.filterNot(isStatic))
    
    // entries
    val ty = new ClassOrInterface(n.getName)
    val entries = n.getEntries.map(e => {
      val init = new ObjectCreation(null, ty, e.getArgs)
      new Field(ModifierSet.FINAL, ty, new Variable(e.getName, init)) })
        
    // conversion function
    val conversion = new Method(IMPLICIT, ty, "convertValue")
    conversion.setBody(new Return(new Cast(ty, "v")))
    conversion.setParameters(new Parameter(valueType, "v") :: Nil)
          
    entries ::: typeDecl :: n.getMembers.filter(isStatic) ::: conversion :: Nil
  }
    
}  