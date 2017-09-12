package com.mysema.scala

import com.mysema.scalagen.{ Scala210, ScalaVersion }
import scala.tools.nsc._
import scala.io.Source.fromFile
import java.io.File

object CompileTestUtils {
  import java.io.File.pathSeparator

  val currentLibraries = (this.getClass.getClassLoader).asInstanceOf[java.net.URLClassLoader].getURLs().toList
  val cp = (jarPathOfClass("scala.tools.nsc.Interpreter") ::
    //scala.ScalaObject was a marker trait used up to 2.10
    (if (ScalaVersion.current <= Scala210) jarPathOfClass("scala.ScalaObject") else "") :: 
    currentLibraries)
  val cpString = cp.mkString(pathSeparator)
     
  private def jarPathOfClass(className: String) = {
    Class.forName(className).getProtectionDomain.getCodeSource.getLocation    
  }
}

trait CompileTestUtils {
  import CompileTestUtils._ 

  def assertCompileSuccess(files: Traversable[File]): Unit = {
    assertCompileSuccess(files
                           map (fromFile(_).mkString)
                           mkString ("\n"))
  }
  
  def assertCompileSuccess(source: String): Unit = {
    val out = new java.io.ByteArrayOutputStream
    val interpreterWriter = new java.io.PrintWriter(out)
    
    val env = new Settings()
    env.classpath.value = cpString
    env.usejavacp.value = true

    val interpreter = new Interpreter(env, interpreterWriter)
    try {
      val result = interpreter.interpret(source.replaceAll("package ", "import "))
      //we have to compare as a string because of an incompatibility between 2.9 and 2.10:
      //in 2.9, result is scala.tools.nsc.InterpreterResults
      //in 2.10, result is scala.tools.nsc.interpreter.Results
      if (result.toString != "Success") {
        throw new AssertionError("Compile failed, interpreter output:\n" + out.toString("utf-8"))
      }
    } finally {
      interpreterWriter.close
      interpreter.close
    }
  }
  
  def recursiveFileList(file: File): Array[File] = {
    if (file.isDirectory) {
      file.listFiles.flatMap(recursiveFileList(_))
    } else {
      Array(file)
    }
  }  
}