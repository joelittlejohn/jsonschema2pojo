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
package com.mysema

import japa.parser.ast.body._
import japa.parser.ast.expr._
import japa.parser.ast.stmt._
import _root_.scala.collection.JavaConversions
import _root_.scala.collection.Set

/**
 * scalagen provides common functionality for this package
 */
package object scalagen {
  
  type JavaCollection[T] = java.util.Collection[T]
  
  type JavaList[T] = java.util.List[T]
  
  type JavaSet[T] = java.util.Set[T]
  
  implicit def toJavaList[T](col: Seq[T]): JavaList[T] = JavaConversions.seqAsJavaList(col) 
  
  implicit def toJavaSet[T](col: Set[T]): JavaSet[T] = JavaConversions.setAsJavaSet(col)
      
//  implicit def toScalaSeq[T](col: JavaList[T]): Seq[T] = {
//    if (col != null) JavaConversions.asScalaBuffer(col) else Nil 
//  }
  
  implicit def toScalaList[T](col: JavaList[T]): List[T] = {
    if (col != null) JavaConversions.asScalaBuffer(col).toList else Nil 
  }
  
  implicit def toScalaSet[T](col: JavaSet[T]): Set[T] = {
    if (col != null) JavaConversions.asScalaSet(col) else Set[T]()
  }
    
}
