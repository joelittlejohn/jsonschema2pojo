package com.mysema.scalagen

import scala.util.{Properties => ScalaProperties}

sealed abstract class ScalaVersion(val sortOrder: Int) extends Ordered[ScalaVersion] {
  def compare(that: ScalaVersion): Int = sortOrder.compare(that.sortOrder)
}

case object Scala29 extends ScalaVersion(0)

case object Scala210 extends ScalaVersion(1)

case object Scala211 extends ScalaVersion(2)

object ScalaVersion {
  lazy val current = {
    //we can't use ScalaProperties.scalaVersionNumber because it's new in 2.10
    val scalaVersionNumber = ScalaProperties.versionString.drop("version ".length)
    getVersion(scalaVersionNumber)
  }
  
  def getVersion(versionNumberString: String) = {
    if (versionNumberString.startsWith("2.9.")) Scala29
    else if (versionNumberString.startsWith("2.10.")) Scala210
    else if (versionNumberString.startsWith("2.11.")) Scala211
    else throw new IllegalArgumentException("Unsupported scala version: " + versionNumberString)
  }
}
