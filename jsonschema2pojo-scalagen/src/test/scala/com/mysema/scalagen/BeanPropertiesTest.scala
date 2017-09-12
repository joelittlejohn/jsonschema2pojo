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
import japa.parser.ast.body.BodyDeclaration
import japa.parser.ast.body.FieldDeclaration
import java.io.FileNotFoundException
import org.junit.Test
import com.mysema.examples._

class BeanPropertiesTest extends AbstractParserTest {

  @Test
  def Methods_Are_Removed1() {
    var unit = getCompilationUnit(classOf[Bean])
    assertEquals(14, unit.getTypes.get(0).getMembers.size)    
    unit = new BeanProperties(Scala211).transform(unit)
    assertEquals(4, unit.getTypes.get(0).getMembers.size)
//    for (member <- unit.getTypes.get(1).getMembers) {
//      assertEquals(classOf[FieldDeclaration], member.getClass)
//    }
  }

  @Test
  def Methods_Are_Removed2() {
    var unit = getCompilationUnit(classOf[Bean2])
    assertEquals(13, unit.getTypes.get(0).getMembers.size)    
    unit = new BeanProperties(Scala211).transform(unit)
    assertEquals(5, unit.getTypes.get(0).getMembers.size)
//    for (member <- unit.getTypes.get(0).getMembers) {
//      assertEquals(classOf[FieldDeclaration], member.getClass)
//    }
  }
}
