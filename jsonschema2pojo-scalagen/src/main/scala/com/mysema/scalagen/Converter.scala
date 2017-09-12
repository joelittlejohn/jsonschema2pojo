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

import java.io.{File, ByteArrayInputStream}
import japa.parser.JavaParser
import japa.parser.ast.{ImportDeclaration, CompilationUnit}
import org.apache.commons.io.FileUtils
import java.util.ArrayList
import japa.parser.ParseException
import java.io.ByteArrayInputStream
import java.util.regex.Pattern

object Converter {
  
  /**
   * default instance for Converter type
   */
  lazy val instance = instance29
  
  /**
   * Converter targeting scala 2.9
   */
  lazy val instance29 = createConverter(Scala29)
  
  /**
   * Converter targeting scala 2.10
   */
  lazy val instance210 = createConverter(Scala210)
  
  /**
   * Converter targeting scala 2.11
   */
  lazy val instance211 = createConverter(Scala211)
  
  def getInstance(version: ScalaVersion) = version match {
    case Scala29 => instance29
    case Scala210 => instance210
    case Scala211 => instance211
  }
  
  /**
   * Converter for the current runtime scala version
   */
  def getInstance(): Converter = {
    getInstance(ScalaVersion.current)
  }
  
  private def createConverter(version: ScalaVersion) = {
    new Converter("UTF-8",List[UnitTransformer](
      Rethrows,
      VarToVal,
      Synchronized,
      RemoveAsserts, 
      new Annotations(version),
      Enums,
      Primitives,
      SerialVersionUID,
      ControlStatements, 
      CompanionObject,
      Underscores,
      Setters,
      new BeanProperties(version), 
      Properties,
      Constructors, 
      Initializers,
      SimpleEquals))
  }
  
}

/**
 * Converter converts Java sources into Scala sources
 */
class Converter(encoding: String, transformers: List[UnitTransformer]) {
    
  def convert(inFolder: File, outFolder: File) {
    val inFolderLength = inFolder.getPath.length + 1
    val inToOut = getJavaFiles(inFolder)
      .map(in => (in, toOut(inFolderLength, outFolder, in))) 
      
    // create out folders
    inToOut.foreach(_._2.getParentFile.mkdirs() )  
    JavaParser.setCacheParser(false)
    inToOut.foreach{ case (in,out) => convertFile(in,out) }
  }
  
  def convertFile(in: File, out: File) {
    try {
      val compilationUnit = JavaParser.parse(in, encoding)
      val sources = toScala(compilationUnit)   
      FileUtils.writeStringToFile(out, sources, "UTF-8")  
    } catch {
      case e: Exception => throw new RuntimeException("Caught Exception for " + in.getPath, e) 
    }    
  }
  
  def convert(javaSource: String, settings: ConversionSettings = ConversionSettings()): String = {
    val compilationUnit = JavaParser.parse(new ByteArrayInputStream(javaSource.getBytes(encoding)), encoding)
    toScala(compilationUnit, settings)
  }
  
  def toScala(unit: CompilationUnit, settings: ConversionSettings = ConversionSettings()): String = {
    if (unit.getImports == null) {
      unit.setImports(new ArrayList[ImportDeclaration]())  
    }    
    val transformed = transformers.foldLeft(unit) { case (u,t) => t.transform(u) }    
    var visitor = new ScalaDumpVisitor(settings)
    transformed.accept(visitor, new ScalaDumpVisitor.Context())
    visitor.getSource
  }
  
  private def toOut(inFolderLength: Int, outFolder: File, in: File): File = {
    val offset = if (in.getName == "package-info.java") 10 else 5
    new File(outFolder, in.getPath.substring(inFolderLength, in.getPath.length-offset)+".scala")
  }
  
  private def getJavaFiles(file: File): Seq[File] = {
    if (file.isDirectory) {
      file.listFiles.toSeq
        .filter(f => f.isDirectory || f.getName.endsWith(".java"))
        .flatMap(f => getJavaFiles(f))
    } else {
      if (file.exists) file :: Nil else Nil
    }
  }
  
}
