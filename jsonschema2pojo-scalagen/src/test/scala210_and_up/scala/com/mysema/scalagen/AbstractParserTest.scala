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

import japa.parser.JavaParser
import japa.parser.ParseException
import japa.parser.ast.{CompilationUnit, ImportDeclaration}
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.ArrayList
import scala.collection.JavaConversions._
import scala.reflect.ClassTag

abstract class AbstractParserTest {
  
  def getCompilationUnit(cl: Class[_]): CompilationUnit = {
    var file = new File("src/test/scala/" + cl.getName.replace('.', '/') + ".java")
    var in = new FileInputStream(file)
    val unit = JavaParser.parse(in)
    if (unit.getImports == null) {
      unit.setImports(new ArrayList[ImportDeclaration])
    }
    unit
  }
  
  def toScala(obj: AnyRef): String = toScala(getCompilationUnit(obj.getClass))
  
  def toScala[T](implicit ct: ClassTag[T]): String = toScala(getCompilationUnit(ct.runtimeClass))
  
  def toScala[T](settings: ConversionSettings)(implicit ct: ClassTag[T]): String =
    toScala(getCompilationUnit(ct.runtimeClass), settings)
  
  def toScala(unit: CompilationUnit): String = Converter.getInstance().toScala(unit)
  
  def toScala(unit: CompilationUnit, settings: ConversionSettings): String = Converter.getInstance().toScala(unit, settings)
  
}
