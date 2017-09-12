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
package com.mysema.scala

import java.beans.Introspector
import org.apache.commons.lang3.StringUtils

object BeanUtils {

  def capitalize(name: String): String = {
    if (name.length > 1 && Character.isUpperCase(name.charAt(1))) {
       name
    } else {
       StringUtils.capitalize(name)
    }
  }

  def uncapitalize(name: String): String = Introspector.decapitalize(name);

}