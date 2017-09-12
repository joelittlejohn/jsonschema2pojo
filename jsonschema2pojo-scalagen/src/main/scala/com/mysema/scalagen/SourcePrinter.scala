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

/**
 * @author tiwe
 *
 */
class SourcePrinter {
  
  private var offset = 0
  
  private var level = 0

  private var indented = false

  private val buf = new StringBuilder()

  def indent() { 
    level += 1 
  }

  def unindent() { 
    level -= 1 
  }

  private def makeIndent() {
    for (i <- 0 until level) { buf.append("  ") }
  }

  def print(arg: String) {
    if (!indented) {
      makeIndent()
      indented = true
    }
    buf.append(arg)
  }

  def printLn(arg: String) {
    print(arg)
    printLn()    
  }

  def printLn() {
    buf.append("\n")
    offset = buf.length
    indented = false
  }

  def source: String = buf.toString
  
  def lineLength = buf.length - offset

  override def toString(): String = source
  
}
