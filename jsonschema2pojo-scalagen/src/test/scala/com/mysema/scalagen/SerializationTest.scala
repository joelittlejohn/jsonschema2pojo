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

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import org.junit.{ Test, Ignore }
import org.junit.Assert._
import japa.parser.JavaParser
import com.mysema.examples._

class SerializationTest extends AbstractParserTest {

  private def assertContains(str: String, strings: String*) {
    strings.foreach { s => assertTrue(s + " was not found", str.contains(s)) }
  }

  @Test
  def AbstractCodeWriter {
    val sources = toScala[AbstractCodeWriter[_]]
    assertContains(sources,
      "abstract class AbstractCodeWriter[T <: AbstractCodeWriter[T]]" +
      "(private val appendable: Appendable, private val spaces: Int)\n    extends Appendable {")
  }

  @Test
  def AbstractDao {
    val sources = toScala[AbstractDao[_]]
    assertContains(sources, "protected def query(): JPQLQuery = new HibernateQuery(getSession)")
  }

  @Test
  def AnnotatedElementAdapter {
    val sources = toScala[AnnotatedElementAdapter]
    assertContains(sources, "for (element <- elements; annotation <- element.getAnnotations) {")
  }

  @Test
  def ArrayConstructorExpression {
    val sources = toScala[ArrayConstructorExpression[_]]
    assertContains(sources,
      "@SerialVersionUID(8667880104290226505L)",
      "val elementType = `type`.getComponentType.asInstanceOf[Class[T]]",
      "override def equals(obj: Any): Boolean =")

  }

  @Test
  def ArrayTests {
    val sources = toScala[ArrayTests]
    assertContains(sources, "def foo(): Array[Int] = Array.ofDim[Int](2)")
  }

  @Test
  def Bean {
    val sources = toScala[Bean]
    assertContains(sources, "@BeanProperty")
  }

  @Test
  def Bean2 {
    val sources = toScala[Bean2]
    assertContains(sources, "@BeanProperty")
  }

  @Test
  def BeanWithUnderscores {
    val sources = toScala[BeanWithUnderscores]
    assertContains(sources,
        "var firstName: String = _",
        "override def toString(): String = firstName + \" \" + this.lastName")
  }

  @Test
  def Casts {
    val sources = toScala[Casts]
    assertContains(sources, "args.length.toDouble")
  }

  @Test
  def ConstantImpl {
    val sources = toScala[ConstantImpl[_]]
    assertContains(sources, "private val BYTES = new Array[Constant[Byte]](CACHE_SIZE)")
  }

  @Test
  def Constructors {
    val sources = toScala[com.mysema.examples.Constructors]
    assertContains(sources, "class Constructors(first: String, last: String) {")
  }

  @Test
  def Constructors2 {
    val sources = toScala[com.mysema.examples.Constructors2]
    assertContains(sources, "class C(private val a: Int)")
  }

  @Test
  def Control {
    val sources = toScala[Control]
    assertContains(sources,
        "for (i <- 0 until integers.size) {",
        "for (i <- integers) {",
        "for (i <- integers if i > 0) {",
        "ints.find(_ == i).getOrElse(-1)",
        "ints.find(_ == i).map(2 * _).getOrElse(-1)",
        "for ((key, value) <- entries) {",
        "println(key + \" \" + value)")
  }

  @Test
  def Dao {
    val sources = toScala[IDao[_,_]]
    assertContains(sources, "trait IDao[Entity, Id <: Serializable] {")
  }

  @Test
  def DateTimeExpression {
    val sources = toScala[DateTimeExpression[_]]
    assertContains(sources,
        "lazy val dayOfMonth = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.DAY_OF_MONTH, this)",
        "lazy val dayOfWeek = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.DAY_OF_WEEK, this)",
        "lazy val dayOfYear = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.DAY_OF_YEAR, this)",
        "lazy val hour = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.HOUR, this)",
        "lazy val minute = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.MINUTE, this)",
        "lazy val second = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.SECOND, this)",
        "lazy val milliSecond = NumberOperation.create(classOf[Integer], Ops.DateTimeOps.MILLISECOND, this)")
  }

  @Test
  def FileSystemRegistry {
    val sources = toScala[FileSystemRegistry]
    assertContains(sources, "class FileSystemRegistry private () {")
  }

  @Test
  def IfElse {
    val sources = toScala[IfElse]
    assertContains(sources, "property = if (System.currentTimeMillis() > 0) \"y\" else \"z\"")
  }

  @Test
  def Immutable {
    val sources = toScala[Immutable]
    assertContains(sources,
        "class Immutable(@BeanProperty val firstName: String, @BeanProperty val lastName: String)",
        "val immutable = new Immutable")
  }

  @Test
  def Immutable2 {
    val sources = toScala[Immutable2]
    assertContains(sources,
        "class Immutable2(@BeanProperty val firstName: String, @BeanProperty val lastName: String)")
  }

  @Test
  def Initializers {

  }

  @Test
  def InnerClasses {
    val sources = toScala[InnerClasses]
    assertContains(sources, "private class LoopContext private () {")
  }

  @Test
  def LongLines {
    val sources = toScala[LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName](ConversionSettings(splitLongLines = false))
    assertContains(sources, "class LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName(nums: Int*) extends LongClassToExtendAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa {")
    assertContains(sources, "var x: LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName = new LongLinesIncludingAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongClassName(444, 2, 3, 4, 5, 6, 7, 8, 9, 10)")
    assertContains(sources, "def this(a: Int, b: Int, c: Int, d: Int, e: Int)")
    assertContains(sources, """if ("very long condition ........................".length > 0 || "other long condition".length > 0)""")
    assertContains(sources, """for (n <- aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa if "long condition goes here ........................".length > 0)""")
  }

  @Test
  def LazyInitBeanAccessor {
    val sources = toScala[LazyInitBeanAccessor]
    assertContains(sources, "lazy val value = \"XXX\"")
  }
  
  @Test
  def Loop {
    val sources = toScala[Loop]
    assertContains(sources, """.find(_ => str.startsWith("a"))""")
    assertContains(sources, ".map(_ => true)")
    assertContains(sources, ".find(str.startsWith(_))")
    assertContains(sources, ".map(_.length > 0)")
    assertContains(sources, ".find(b => str.startsWith(b) && b.length < 10)")
    assertContains(sources, ".map(b => b.length > 0 || b.length < 15)")
  }

  @Test
  def Modifiers {
    val sources = toScala[Modifiers]
    assertContains(sources,
        "@transient private var foo: String = \"foo\"",
        "@volatile private var bar: String = \"bar\"")
  }

  @Test
  def Ops {
    val sources = toScala[Ops]
    assertContains(sources, "object Ops {", "object AggOps {")
  }

  @Test
  def Protected {
    val sources = toScala[Protected]
    assertContains(sources, "class Protected protected ()")
  }

  @Test
  def Reserved {
    val sources = toScala[Reserved]
    assertContains(sources, "`object`","`type`","`var`","`val`")
  }

  @Test
  def Resource {
    val sources = toScala[Resource]
    assertContains(sources, "case o: Resource => o.path == path")
  }

  @Test
  def Returns {
    val sources = toScala[Returns]
    assertContains(sources,"(start until n).find(_ / 5 > 1).getOrElse(-1)")
      //"for (i <- start until n if i / 5 > 1) return i",)
  }

  @Test
  def SimpleCompiler {
    val sources = toScala[SimpleCompiler]
    assertContains(sources,
      "for (url <- classLoader.asInstanceOf[URLClassLoader].getURLs) {",
      //"case e: UnsupportedEncodingException => throw new RuntimeException(e)",
      "this(ToolProvider.getSystemJavaCompiler, Thread.currentThread().getContextClassLoader)")
  }

  @Test
  def SourceFileObject {

  }

  @Test
  def SuperConstructors {
    val sources = toScala[SuperConstructors]
    assertContains(sources,
      "class SuperConstructors(first: String, last: String) extends SuperClass(first) {")
  }

  @Test
  def SwitchCase {
    val sources = toScala[SwitchCase]
    assertContains(sources,
      "case 0 => println(0)",
      "case 1 => println(1)",
      "case 0 | 1 => println(1)")
  }

  @Test
  def Static {
    val sources = toScala[Static]
    assertContains(sources,
      "def main(args: Array[String])",
      "def main2(args: Array[String])")
  }

  @Test
  def TryCatch {
    val sources = toScala[TryCatch]
    assertContains(sources,
      "case e: IllegalArgumentException => throw new RuntimeException(e)",
      "case e: NullPointerException => System.err.println(e.getMessage)")

  }

  @Test @Ignore // FIXME
  def WithComments {
    val sources = toScala[WithComments]
    assertContains(sources, "javadocs", "// comments inside")
  }

  @Test
  def WithStatic {
    val sources = toScala[WithStatic]
    assertContains(sources, "object WithStatic {")
  }

  @Test
  def WithStaticAndInstance {
    val sources = toScala[WithStaticAndInstance]
    assertContains(sources,
      "object WithStaticAndInstance {",
      "class WithStaticAndInstance {")
  }

  @Test
  def Wildcard {
    val sources = toScala[Wildcard]
    assertContains(sources, "def bar(list: List[_ <: CharSequence]): Int = list.size")
  }

}