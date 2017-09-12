/*
 * Copyright (C) 2011, James McMahon
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

/**
 * Simple harness to facilitate running scalagen from the command line
 */
object Cli {
  val usage = "USAGE: scalagen <src-directory> <target-directory>"

  def main(args: Array[String]) {
    if (args.length != 2) {
      println(usage)
      return
    }

    val in = new File(args(0))
    if (in.exists) {
      val out = new File(args(1))
      Converter.instance.convert(in, out)
    }
  }
}
