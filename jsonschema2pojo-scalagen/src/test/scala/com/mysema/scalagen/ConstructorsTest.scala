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

import org.junit.Assert.assertEquals
import japa.parser.ParseException
import japa.parser.ast.CompilationUnit
import java.io.FileNotFoundException
import org.junit.Test
import com.mysema.examples._

class ConstructorsTest extends AbstractParserTest {

  @Test
  def Empty_Constructor_Are_Ignored() {
    var unit = getCompilationUnit(classOf[WithStatic])
    assertEquals(2, unit.getTypes.get(0).getMembers.size)
    unit = Constructors.transform(unit)
    assertEquals(2, unit.getTypes.get(0).getMembers.size)
  }

  @Test
  def Body_Is_Extracted() {
    var unit = getCompilationUnit(classOf[Immutable])
    assertEquals(6, unit.getTypes.get(0).getMembers.size)
    unit = Constructors.transform(unit)
    assertEquals(4, unit.getTypes.get(0).getMembers.size)
  }
  
  @Test
  def Immutable2 {
    var unit = getCompilationUnit(classOf[Immutable2])
    unit = Constructors.transform(unit)
    // TODO
  }
  
}
