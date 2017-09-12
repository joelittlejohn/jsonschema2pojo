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

object ControlStatements extends ControlStatements

/**
 * ControlStatements transform ForStmt, SwitchEntryStmt and If statements
 */
class ControlStatements extends UnitTransformerBase {
  
  private val KEY = new Name("key")
  
  private val VALUE = new Name("value")
  
  private val toUnderscore = new ModifierVisitor[Set[String]] {    
    override def visitName(n: String, arg: Set[String]): String = {
      if (arg.contains(n)) "_" else n
    }  
  }
  
  private def numMatchingNames(n: Node, variableName: String): Int = {
    var matched = 0
    val visitor = new ModifierVisitor[Null] {
      override def visitName(n: String, dummy: Null): String = {
        if (n == variableName) matched += 1
        n
      }
    }
    n.accept(visitor, null)
    matched
  }
  
  private val toKeyAndValue = new ModifierVisitor[String] {
    override def visit(nn: MethodCall, arg: String): Node = {
      val n = super.visit(nn, arg).asInstanceOf[MethodCall]
      n match {
        case MethodCall(str(`arg`), "getKey", Nil) => KEY
        case MethodCall(str(`arg`), "getValue", Nil) => VALUE
        case _ => n
      }
    }    
  }
     
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
        
  override def visit(nn: For, arg: CompilationUnit): Node = {
    // transform
    //   for (int i = 0; i < x; i++) block 
    // into
    //   for (i <- 0 until x) block
    val n = super.visit(nn, arg).asInstanceOf[For]    
    n match {
      case For((init: VariableDeclaration) :: Nil, l lt r, incr(_) :: Nil, _) => {
        val until = new MethodCall(init.getVars.get(0).getInit, "until", r :: Nil)
        init.getVars.get(0).setInit(null)
        new Foreach(init, until, n.getBody)
      }
      case _ => n
    }
  }
  
  override def visit(nn: MethodCall, arg: CompilationUnit): Node = {
    // transform
    //   System.out.println
    // into 
    //   println
    val n = super.visit(nn, arg).asInstanceOf[MethodCall]
    n match {
      case MethodCall(str("System.out"), "println", args) => {
        new MethodCall(null, "println", args)
      }
      case _ => n
    }
  }
  
  override def visit(nn: Foreach, arg: CompilationUnit): Node = {
    val n = super.visit(nn, arg).asInstanceOf[Foreach]
    n match {
      case Foreach(
          VariableDeclaration(t, v :: Nil), 
          MethodCall(scope, "entrySet", Nil), body) => {
        val vid = v.getId.toString
        new Foreach(
            VariableDeclaration(0, "(key, value)", Type.Object), 
            scope, n.getBody.accept(toKeyAndValue, vid).asInstanceOf[Statement])            
      }
      case _ => n
    }    
  }
  
  // TODO : maybe move this to own class
  override def visit(nn: Block, arg: CompilationUnit): Node = {
    // simplify
    //   for (format <- values if format.mimetype == contentType) return format
    //   defaultFormat
    // into
    //   values.find(_.mimetype == contenType).getOrElse(defaultFormat)
    val n = super.visit(nn, arg).asInstanceOf[Block]
    n match {
      case Block( 
          Foreach(v, it, If(cond, Return(rv1), null)) ::
          Return(rv2) :: Nil) => createFindCall(it, v, cond, rv1, rv2)
      case _ => n
    }
  }
  
  private def createClosure(vid: String, expr: Expression): List[Expression] = numMatchingNames(expr, vid) match {
    case 0 => List(new BeginClosureExpr("_"), expr)
    case 1 => List(expr.accept(toUnderscore, Set(vid)).asInstanceOf[Expression])
    case _ => List(new BeginClosureExpr(vid), expr)
  }
  
  private def createFindCall(it: Expression, v: VariableDeclaration, 
      cond: Expression, rv1: Expression, rv2: Expression): Statement = {
    val vid = v.getVars.get(0).getId.toString
    val newCond = createClosure(vid, cond)
    val newIt = it match {
      case MethodCall(_, "until", _ :: Nil) => new Enclosed(it)
      case _ => it
    }
    val findCall = new MethodCall(newIt, "find", newCond)
    val expr = if (vid == rv1.toString) findCall
               else new MethodCall(findCall, "map", createClosure(vid, rv1))
    val getOrElse = new MethodCall(expr, "getOrElse", rv2 :: Nil)
    new Block(new ExpressionStmt(getOrElse) :: Nil)
  } 
  
  override def visit(nn: If, arg: CompilationUnit): Node = {
    // transform
    //   if (condition) target = x else target = y
    // into
    //   target = if (condition) e else y    
    val n = super.visit(nn, arg).asInstanceOf[If]    
    n match {
      case If(cond, Stmt(t1 set v1), Stmt(t2 set v2)) if t1 == t2 => {
        new ExpressionStmt(new Assign(t1, new Conditional(n.getCondition, v1, v2), Assign.assign))  
      }
      case _ => n
    }    
  }
  
  override def visit(nn: SwitchEntry, arg: CompilationUnit) = {    
    // remove break
    val n = super.visit(nn, arg).asInstanceOf[SwitchEntry]
    val size = if (n.getStmts == null) 0 else n.getStmts.size
    if (size > 1 && n.getStmts.get(size-1).isInstanceOf[Break]) {
      //n.getStmts.remove(size-1)
      n.setStmts(n.getStmts.dropRight(1))
    }
    n
  }
    
}