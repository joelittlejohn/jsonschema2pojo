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

import com.mysema.scala.BeanUtils
import Types._

/**
 * 
 */
trait BeanHelpers extends Helpers {
    
  private val getter = "get\\w+".r
  
  private val setter = "set\\w+".r
  
  private val booleanGetter = "is\\w+".r
  
  def isBeanGetter(method: Method): Boolean = method match {
    case Method(getter(_*), t, Nil, Return(field(_))) => true
    case Method(getter(_*), t, Nil, b: Block) => isLazyCreation(b, getProperty(method))
    case _ => false
  }
  
  def isBooleanBeanGetter(method: Method): Boolean = method match {
    case Method(booleanGetter(_*), Type.Boolean, Nil, Return(field(_))) => true
    case Method(booleanGetter(_*), Type.Boolean, Nil, b: Block) => isLazyCreation(b, getProperty(method))
    case _ => false
  }
      
  def isBeanSetter(method: Method): Boolean = method match {
    case Method(setter(_*), Type.Void, _ :: Nil, Stmt(_ set _)) => true
    case _ => false
  }  
  
  def getProperty(method: Method) = {
    val name = method.getName
    BeanUtils.uncapitalize(name.substring(if (name.startsWith("is")) 2 else 3))
  }
}