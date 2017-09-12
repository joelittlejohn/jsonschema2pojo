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

import japa.parser.ast.Node
import japa.parser.ast.body._
import japa.parser.ast.expr._
import japa.parser.ast.stmt._
import japa.parser.ast.`type`._
import japa.parser.ast.visitor.ModifierVisitorAdapter
import japa.parser.ast.CompilationUnit
import japa.parser.ast.ImportDeclaration
import java.util.ArrayList

object Types extends Types

/**
 * Types contains type aliases and extractor functionality
 */
trait Types {
  
  def extract(stmt: Statement): Statement = stmt match {
    case b: Block => if (b.getStmts != null && b.getStmts.size == 1) b.getStmts.get(0) else b
    case _ => stmt
  } 
    
  //private def safeToString(obj: AnyRef): String = if (obj != null) obj.toString else null
  
  private def handle(o: BinaryExpr.Operator, b: BinaryExpr) = {
    if (b.getOperator == o) Some(b.getLeft, b.getRight) else None 
  }
    
  object str {
    def unapply(n: Node) = if (n != null) Some(n.toString) else None
  }
  
  object and {
    def unapply(b: Binary) = handle(Binary.and, b)    
  }
  
  object or {
    def unapply(b: Binary) = handle(Binary.or, b)    
  } 
  
  object set {
    def unapply(a: Assign) = if (a.getOperator == Assign.assign) Some(a.getTarget, a.getValue) else None
  }
    
  object === {
    def unapply(b: Binary) = handle(Binary.equals, b) 
  }
  
  object incr {
    def unapply(u: Unary) = if (u.getOperator.toString.endsWith("Increment")) Some(u.getExpr) else None
  }
  
  object lt {
    def unapply(b: Binary) = handle(Binary.less, b)
  }
    
  object field {
    def unapply(f: Expression) = f match {
      case FieldAccess(str("this"), field) => Some(field)
      case Name(field) => Some(field)
      case _ => None
    }
  }
  
  object isnull {
    def unapply(b: Binary) = {
      if (b.getOperator == Binary.equals && b.getRight.isInstanceOf[Null]) Some(b.getLeft)
      else None        
    }
  }
    
  object Assign {
    val assign = AssignExpr.Operator.assign
    def unapply(a: Assign) = Some(a.getOperator, a.getTarget, a.getValue)
  }
    
  object Binary {
    val or = BinaryExpr.Operator.or
    val and = BinaryExpr.Operator.and
    val equals = BinaryExpr.Operator.equals
    val notEquals = BinaryExpr.Operator.notEquals
    val less = BinaryExpr.Operator.less
    val greater = BinaryExpr.Operator.greater
    def unapply(b: Binary) = Some(b.getOperator, b.getLeft, b.getRight)    
  }
    
  object Block {
    //def unapply(b: Block) = Some(if (b != null) toScalaList(b.getStmts) else Nil)
    def unapply(s: Statement) = s match {
      case b: Block => Some(if (b != null) toScalaList(b.getStmts) else Nil)
      case _ => Some(List(s))
    } 
  }
  
  object Cast {
    def unapply(c: Cast) = Some(c.getExpr, c.getType)
  }
  
  object Catch {
    def unapply(c: Catch) = Some(c.getExcept, extract(c.getCatchBlock))
  }
  
  object ClassOrInterface {
    def unapply(c: ClassOrInterfaceDecl) = Some(c.getName, toScalaList(c.getMembers))
  }
  
  object Conditional {
    def unapply(c: Conditional) = Some(c.getCondition, c.getThenExpr, c.getElseExpr)
  }
  
  object Constructor {
    def unapply(c: Constructor) = Some(toScalaList(c.getParameters), extract(c.getBlock))
    def unapply(c: ConstructorInvocation) = Some(c.isThis, toScalaList(c.getArgs))
  }
  
  object Enclosed {
    def unapply(e: Enclosed) = Some(e.getInner)
  }
  
  object FieldAccess {
    def unapply(f: FieldAccess) = Some(f.getScope, f.getField)
  }
  
  object For {
    def unapply(f: For) = Some(toScalaList(f.getInit), f.getCompare, toScalaList(f.getUpdate), extract(f.getBody))
  }
  
  object Foreach {
    def unapply(f: Foreach) = Some(f.getVariable, f.getIterable, extract(f.getBody))
  }
  
  object If {
    def unapply(i: If) = Some(i.getCondition, extract(i.getThenStmt), extract(i.getElseStmt))
  }
  
  object InstanceOf {
    def unapply(i: InstanceOf) = Some(i.getExpr, i.getType)
  }
  
  object Initializer {
    def unapply(i: Initializer) = Block.unapply(i.getBlock)
  }
    
  object Literal {
    def unapply(l: Literal) = l match {
      case b: BooleanLiteral => Some(b.getValue)
    }
  }
  
  object Method {
    def unapply(m: Method) = Some(m.getName, m.getType, toScalaList(m.getParameters), extract(m.getBody))    
  }
  
  object MethodCall {
    def unapply(m: MethodCall) = Some(m.getScope, m.getName, toScalaList(m.getArgs))
  }
  
  object Name {
    def unapply(n: Name) = Some(n.getName)
  }
  
  object Parameter {
    def unapply(p: Parameter) = Some(p.getId.getName)
  }
    
  object Return {
    def unapply(r: Return) = Some(r.getExpr)
  }
  
  object Stmt {
    def unapply(s: ExpressionStmt) = Some(s.getExpression)
  }
  
  object This {
    def unapply(t: This) = Some(t.getClassExpr)
  }
  
  object Type {
    val Boolean = new PrimitiveType(PrimitiveType.Primitive.Boolean)
    val Int = new PrimitiveType(PrimitiveType.Primitive.Int)
    val Object = new ReferenceType(new ClassOrInterfaceType("Object"))
    val String = new ReferenceType(new ClassOrInterfaceType("String"))
    val Void = new VoidType()
  }
    
  object Unary {
    val positive = UnaryExpr.Operator.positive
    val negative = UnaryExpr.Operator.negative
    val preIncrement = UnaryExpr.Operator.preIncrement
    val preDecrement = UnaryExpr.Operator.posDecrement
    val not = UnaryExpr.Operator.not
    val inverse = UnaryExpr.Operator.inverse
    val posIncrement = UnaryExpr.Operator.posIncrement
    val posDecrement = UnaryExpr.Operator.posDecrement
    def unapply(u: Unary) = Some(u.getOperator, u.getExpr)
  }
  
  object Variable {
    def unapply(v: VariableDeclarator) = Some(v.getId.getName, v.getInit)    
  }
  
  object VariableDeclaration {
    def apply(mod: Int, name: String, t: Type): VariableDeclaration = {
      val variable = new VariableDeclarator(new VariableDeclaratorId(name))
      new VariableDeclaration(mod, t, variable :: Nil)
    }
    def unapply(v: VariableDeclaration) = Some(v.getType, toScalaList(v.getVars))
  }
  
  type Annotation = AnnotationExpr 
  
  type AnnotationDecl = AnnotationDeclaration
  
  type AnnotationMember = AnnotationMemberDeclaration
  
  type Assign = AssignExpr
  
  type Binary = BinaryExpr
    
  type Block = BlockStmt
  
  type BodyDecl = BodyDeclaration
  
  type BooleanLiteral = BooleanLiteralExpr
  
  type Break = BreakStmt
    
  type Cast = CastExpr
  
  type Catch = CatchClause
  
  type ClassOrInterfaceDecl = ClassOrInterfaceDeclaration
  
  type ClassOrInterface = ClassOrInterfaceType
  
  type CompilationUnit = japa.parser.ast.CompilationUnit
  
  type Conditional = ConditionalExpr
    
  type Constructor = ConstructorDeclaration
  
  type ConstructorInvocation = ExplicitConstructorInvocationStmt
  
  type Enclosed = EnclosedExpr
  
  type EnumDecl = EnumDeclaration
  
  type Expression = japa.parser.ast.expr.Expression
  
  type ExpressionStmt = japa.parser.ast.stmt.ExpressionStmt
  
  type Field = FieldDeclaration
    
  type FieldAccess = FieldAccessExpr
    
  type For = ForStmt
  
  type Foreach = ForeachStmt
    
  type If = IfStmt
  
  type Import = ImportDeclaration
  
  type InstanceOf = InstanceOfExpr
  
  type Initializer = InitializerDeclaration
    
  type Literal = LiteralExpr
  
  type MarkerAnnotation = MarkerAnnotationExpr
    
  type Method = MethodDeclaration
  
  type MethodCall = MethodCallExpr
    
  type Name = NameExpr
    
  type Node = japa.parser.ast.Node
  
  type Null = NullLiteralExpr
  
  type ObjectCreation = ObjectCreationExpr
    
  type Parameter = japa.parser.ast.body.Parameter
  
  type Return = ReturnStmt
  
  type SingleMemberAnnotation = SingleMemberAnnotationExpr

  type Statement = japa.parser.ast.stmt.Statement
  
  type Switch = SwitchStmt
  
  type SwitchEntry = SwitchEntryStmt
  
  type SynchronizedStmt = japa.parser.ast.stmt.SynchronizedStmt
  
  type This = ThisExpr
  
  type Throw = ThrowStmt
  
  type Try = TryStmt
  
  type Type = japa.parser.ast.`type`.Type

  type TypeDecl = japa.parser.ast.body.TypeDeclaration
  
  type Unary = UnaryExpr
  
  type Variable = VariableDeclarator
  
  type VariableDeclaration = VariableDeclarationExpr
  
  //type VariableDeclarator = japa.parser.ast.body.VariableDeclarator
  
  type VariableDeclaratorId = japa.parser.ast.body.VariableDeclaratorId

  type VoidType = japa.parser.ast.`type`.VoidType
  
}
