package com.mysema.scalagen

//import japa.parser.ast.`type`._
import org.junit.Test
import org.junit.Assert._
import UnitTransformer._
import java.util.Collections

class HelpersTest {
  
  val helpers = new AnyRef with Helpers
  
  @Test
  def IsHashCode {
    val method = new Method(0, Type.Int, "hashCode", null)
    assertTrue(helpers.isHashCode(method))
  }
  
  @Test
  def IsEquals {
    val method = new Method(0, Type.Boolean, "equals", Collections.singletonList[Parameter](new Parameter))
    assertTrue(helpers.isEquals(method))
  }
  
  @Test
  def ToString {
    val method = new Method(0, Type.String, "toString", null)
    assertTrue(helpers.isToString(method))
  }
  
  
}