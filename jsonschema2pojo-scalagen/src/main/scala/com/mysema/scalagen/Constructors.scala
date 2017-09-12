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
import java.util.ArrayList
import UnitTransformer._

object Constructors extends Constructors

/**
 * Constructors reorders and normalizes constructors
 */
class Constructors extends UnitTransformerBase {

  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit]
  }

  override def visit(n: ClassOrInterfaceDecl, cu: CompilationUnit):  ClassOrInterfaceDecl = {
    val t = super.visit(n, cu).asInstanceOf[ClassOrInterfaceDecl]
    // make members list mutable
    t.setMembers(new ArrayList[BodyDecl](t.getMembers))

    // get all constructors
    val constr = t.getMembers.collect { case c: Constructor => c }

    if (constr.isEmpty) {
      return t
    }

    // get first without delegating
    val first = constr.find( c =>
      c.getBlock.isEmpty || !isThisConstructor(c.getBlock()(0)))

    // move in front of others
    first.filter(_ != constr(0)).foreach { c =>
      t.getMembers.remove(c)
      t.getMembers.add(t.getMembers.indexOf(constr(0)), c)
    }

    // copy initializer, if constructor block has non-constructor statements
    val c = first.getOrElse(constr(0))

    // add empty constructor invocation for all other constructors without
    // constructor invocations
    constr.filter(_ != c).foreach { c =>
      if (c.getBlock.isEmpty) {// || !c.getBlock()(0).isInstanceOf[ConstructorInvocation]) {
        c.getBlock.add(new ConstructorInvocation(true, null, null))
      }
    }

    if (!c.getBlock.isEmpty &&
        !c.getBlock.getStmts.filter(!_.isInstanceOf[ConstructorInvocation]).isEmpty) {

      processStatements(cu, t, c)

      if (!c.getBlock.isEmpty &&
          !(c.getBlock.size == 1 && c.getBlock()(0).isInstanceOf[ConstructorInvocation] &&
          !c.getBlock()(0).asInstanceOf[ConstructorInvocation].isThis())) {
        val initializer = new Initializer(false, c.getBlock)
        t.getMembers.add(t.getMembers.indexOf(c), initializer)
      }

    }

    // add missing delegations
    t.getMembers.collect { case c: Constructor => c }.filter(_ != c)
      .foreach { c =>
        if (!c.getBlock.isEmpty && !c.getBlock()(0).isInstanceOf[ConstructorInvocation]) {
          //c.getBlock.getStmts.add(0, new ConstructorInvocation(true, null, null))
          c.getBlock.setStmts(new ConstructorInvocation(true, null, null) :: c.getBlock.getStmts)
        }
      }
    t
  }

  private def processStatements(cu: CompilationUnit, t: TypeDecl, c: Constructor) {
    val fields = t.getMembers.collect { case f: Field => f }
    val variables = fields.flatMap(_.getVariables).map(v => (v.getId.getName, v)).toMap
    val variableToField = fields.flatMap(f => f.getVariables.map(v => (v.getId.getName,f)) ).toMap

    var replacements = Map[String, String]()
    
    // go through statements and map assignments to variable initializers
    c.getBlock.getStmts.collect { case s: ExpressionStmt => s }
      .filter(isAssignment(_))
      .foreach { s =>
      val assign = s.getExpression.asInstanceOf[Assign]
      if (assign.getTarget.isInstanceOf[FieldAccess]) {
        val fieldAccess = assign.getTarget.asInstanceOf[FieldAccess]
        processFieldAssign(s, assign, fieldAccess, c, variables, variableToField)
      } else if (assign.getTarget.isInstanceOf[Name]) {
        val namedTarget = assign.getTarget.asInstanceOf[Name]
        if (variables.contains(namedTarget.getName)) {
          if (assign.getValue.isInstanceOf[Name]) { // field = parameter
            val namedValue = assign.getValue.asInstanceOf[Name]
            c.getParameters.find(_.getId.getName == namedValue.getName).foreach { param =>
              val field = variableToField(namedTarget.getName)
              // rename parameter to field name
              param.setId(namedTarget.getName)
              replacements = replacements.+((param.getId.getName, namedTarget.getName))
              copyAnnotationsAndModifiers(field, param)
              // remove field
              field.setVariables(field.getVariables.filterNot(_ == variables(namedTarget.getName)))
            }
          } else { // field = ?!?
            variables(namedTarget.getName).setInit(assign.getValue)
          }
          c.getBlock.remove(s)
        }
      }
    }

    // remove empty field declarations
    fields.filter(_.getVariables.isEmpty).foreach { t.getMembers.remove(_) }
    
    // modify variables in other statements
    val renameTransformer = new RenameTransformer(replacements)
    c.getBlock.setStmts(c.getBlock.getStmts.map(stmt => {
      if (!stmt.isInstanceOf[ExpressionStmt]) {
        stmt.accept(renameTransformer, cu).asInstanceOf[Statement]
      } else {
        stmt
      }
    }))

  }

  private def processFieldAssign(s: ExpressionStmt, assign: Assign, fieldAccess: FieldAccess,
      c: Constructor, variables: Map[String, Variable], variableToField: Map[String, Field] ) {
    if (fieldAccess.getScope.isInstanceOf[This] &&
        variables.contains(fieldAccess.getField)) {
      if (fieldAccess.getField == assign.getValue.toString) {
        val field = variableToField(fieldAccess.getField)
        c.getParameters.find(_.getId.getName == fieldAccess.getField)
          .foreach(copyAnnotationsAndModifiers(field,_))
        // remove field, as constructor parameter can be used
        //field.getVariables.remove(variables(fieldAccess.getField))
        field.setVariables(field.getVariables.filterNot(_ == variables(fieldAccess.getField)))

      } else {
        // remove statement, put init to field
        variables(fieldAccess.getField).setInit(assign.getValue)
      }
      c.getBlock.remove(s)
    }
  }

  private def copyAnnotationsAndModifiers(f: Field, p: Parameter) {
    if (f.getAnnotations != null) {
      p.setAnnotations(p.getAnnotations.union(f.getAnnotations))
    }

    val modifiers = f.getModifiers.addModifier(PROPERTY)
    p.setModifiers(modifiers)
  }

}
