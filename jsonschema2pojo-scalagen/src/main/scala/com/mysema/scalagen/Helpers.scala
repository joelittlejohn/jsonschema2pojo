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

import java.lang.reflect.Modifier
import japa.parser.ast.body.ModifierSet
import _root_.scala.collection.JavaConversions
import _root_.scala.collection.Set
import java.util.ArrayList

/**
 * Common helper methods for transformers and ScalaDumpVisitor
 */
trait Helpers {
  import Types._
  
  val PRIVATE = Modifier.PRIVATE
  val PROPERTY = 0x00001000
  val LAZY     = 0x00002000
  val OBJECT   = 0x00004000
  val IMPLICIT = 0x00008000  
  
  implicit def toRichModifiers(i: Int) = new RichModifiers(i)
  
  class RichModifiers(i: Int) {
    def isAbstract = ModifierSet.isAbstract(i)
    def isFinal = ModifierSet.isFinal(i)
    def isImplicit = ModifierSet.hasModifier(i, IMPLICIT)
    def isLazy = ModifierSet.hasModifier(i, LAZY)
    def isNative = ModifierSet.isNative(i)
    def isObject = ModifierSet.hasModifier(i, OBJECT)
    def isPrivate = ModifierSet.isPrivate(i)
    def isProtected = ModifierSet.isProtected(i)
    def isProperty = ModifierSet.hasModifier(i, PROPERTY)
    def isPublic = ModifierSet.isPublic(i)
    def isStatic = ModifierSet.isStatic(i)
    def isStrictfp = ModifierSet.isStrictfp(i)
    def isSynchronized = ModifierSet.isSynchronized(i)
    def isTransient = ModifierSet.isTransient(i)
    def isVolatile = ModifierSet.isVolatile(i)
    def hasModifier(mod: Int) = ModifierSet.hasModifier(i,mod)
    def addModifier(mod: Int) = ModifierSet.addModifier(i,mod)
    def removeModifier(mod: Int) = ModifierSet.removeModifier(i,mod)    
  }  
  
  type WithModifiers = { def getModifiers(): Int ; def setModifiers(v: Int): Unit }
  
  implicit def toRichWithModifiers(wm: WithModifiers) = new RichWithModifiers(wm)
  
  class RichWithModifiers(wm: WithModifiers) {
    def addModifier(mod: Int): RichWithModifiers = {
      wm.setModifiers(ModifierSet.addModifier(wm.getModifiers, mod))
      this
    } 
    def removeModifier(mod: Int): RichWithModifiers = {
      wm.setModifiers(ModifierSet.removeModifier(wm.getModifiers, mod))
      this
    }
  }
  
  implicit def toRichBlock(b: Block) = new RichBlockStmt(b)
  
  class RichBlockStmt(b: Block) {    
    def apply(i: Int) = if (isEmpty) null else b.getStmts.get(i)
    def isEmpty = b.getStmts == null || b.getStmts.isEmpty
    def add(s: Statement) {
      b.setStmts(b.getStmts :+ s)
    }
    def addAll(s: List[Statement]) {
      b.setStmts(b.getStmts ++ s)
    }
    def remove(s: Statement) {
      b.setStmts(b.getStmts.filterNot(_ == s))
    }
    def removeAll(s: List[Statement]) {
      b.setStmts(b.getStmts.filterNot(s.contains))
    }
    def copy(): Block = {
      def block = new Block()
      def stmts = new ArrayList[Statement]()
      stmts.addAll(b.getStmts)
      block.setStmts(stmts)
      block
    }
    
    def size = if (b.getStmts != null) b.getStmts.size else 0
  }  
    
  //@inline
  def isEmpty(col: JavaCollection[_]): Boolean = col == null || col.isEmpty
  
  def getAssignment(s: Statement): Assign = s match {
    case Stmt(a: Assign) => a
    case _ => null
  }
  
  // TODO use pattern matching
  def getLazyInit(block: Block) = {
    block.getStmts.get(0).asInstanceOf[If]
      .getThenStmt.asInstanceOf[Block]
      .getStmts().get(0).asInstanceOf[ExpressionStmt]
      .getExpression.asInstanceOf[Assign]
      .getValue
  }
    
  def isLazyCreation(block: Block, f: String): Boolean = block match {
    case Block(
        If(isnull(field(`f`)), Stmt(field(`f`) set init), null) :: 
        Return(field(`f`)) :: Nil) => true
    case _ => false   
  }
        
  def isAssignment(s: Statement): Boolean = s match {
    case Stmt(_ set _) => true
    case _ => false
  }
    
  def isThisConstructor(s: Statement): Boolean = s match {
    case ci: ConstructorInvocation => ci.isThis
    case _ => false
  }
  
  def isStatic(member: BodyDecl): Boolean = member match {
    case t: ClassOrInterfaceDecl => t.getModifiers.isStatic || t.getModifiers.isObject || t.isInterface
    case t: TypeDecl => t.getModifiers.isStatic || t.getModifiers.isObject
    case f: Field => f.getModifiers.isStatic
    case m: Method => m.getModifiers.isStatic
    case i: Initializer => i.isStatic
    case _ => false
  }  
  
  def isHashCode(n: Method): Boolean = n match { 
    case Method("hashCode", Type.Int, Nil, _) => true
    case _ => false
  }
    
  def isEquals(n: Method): Boolean = n match {
    case Method("equals", Type.Boolean,_ :: Nil, _) => true
    case _ => false
  }
    
  def isReturnFieldStmt(stmt: Statement): Boolean = stmt match {
    case Return(field(_)) => true
    case _ => false
  }
  
  def isSetFieldStmt(stmt: Statement): Boolean = stmt match {
    case Stmt(_ set _) => true
    case _ => false
  }
  
  def isToString(n: Method): Boolean = n match {
    case Method("toString", Type.String, Nil, _) => true
    case _ => false
  }  
  
      
}