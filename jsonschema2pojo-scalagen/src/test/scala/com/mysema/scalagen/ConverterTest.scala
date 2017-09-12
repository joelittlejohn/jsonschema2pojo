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
import org.junit.Test
import org.junit.Assert._

class ConverterTest extends AbstractParserTest {
  
  @Test
  def Convert_Creates_Files {       
    Converter.instance.convert(new File("src/test/scala"), new File("target/test/scala"))    
    assertTrue(new File("target/test/scala/com/mysema/examples/Bean.scala").exists)
    assertTrue(new File("target/test/scala/com/mysema/examples/Bean2.scala").exists)
  }
  
  @Test
  def Convert_Creates_File_with_Content {
    Converter.instance.convert(new File("src/test/scala"), new File("target/test2/scala"))    
    assertTrue(new File("target/test2/scala/com/mysema/examples/Bean.scala").length > 0)
  }

  @Test
  def Convert_String_Has_Content {
    assertTrue(Converter.instance.convert("class A {}").length > 0)
  }
  
}