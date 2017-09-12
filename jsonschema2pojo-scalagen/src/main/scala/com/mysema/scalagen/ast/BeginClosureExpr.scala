package com.mysema.scalagen.ast

import japa.parser.ast.expr.NameExpr

class BeginClosureExpr(params: String) extends NameExpr(params) {
  def params = getName
}