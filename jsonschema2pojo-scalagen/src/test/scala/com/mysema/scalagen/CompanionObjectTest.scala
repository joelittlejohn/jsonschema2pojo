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

import org.junit.Assert._
import java.io.FileNotFoundException
import japa.parser.ParseException
import japa.parser.ast.CompilationUnit
import org.junit.Test
import com.mysema.examples._
import UnitTransformer._

class CompanionObjectTest extends AbstractParserTest {

  @Test
  def Replace_Class() {
    var unit = getCompilationUnit(classOf[WithStatic])
    assertEquals(1, unit.getTypes.size)
    unit = CompanionObject.transform(unit)
    assertEquals(1, unit.getTypes.size)
    assertEquals(OBJECT, unit.getTypes.get(0).getModifiers)
    assertEquals("WithStatic", unit.getTypes.get(0).getName)
  }

  @Test
  def Replace_Class_Imports() {
    var unit = getCompilationUnit(classOf[WithStatic])
    assertEquals(0, if (unit.getImports == null) 0 else unit.getImports.size)
    unit = CompanionObject.transform(unit)
    assertEquals(0, if (unit.getImports == null) 0 else unit.getImports.size)
  }

  @Test
  def Split_Class() {
    var unit = getCompilationUnit(classOf[WithStaticAndInstance])
    assertEquals(1, unit.getTypes.size)
    unit = CompanionObject.transform(unit)
    assertEquals(2, unit.getTypes.size)
  }

  @Test
  def Split_Class_Imports() {
    var unit = getCompilationUnit(classOf[WithStaticAndInstance])
    assertEquals(0, if (unit.getImports == null) 0 else unit.getImports.size)
    unit = CompanionObject.transform(unit)
    assertEquals(1, if (unit.getImports == null) 0 else unit.getImports.size)
  }
}
