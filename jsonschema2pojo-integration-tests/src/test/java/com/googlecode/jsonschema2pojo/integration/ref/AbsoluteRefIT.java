/**
 * Copyright © 2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.jsonschema2pojo.integration.ref;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class AbsoluteRefIT {

    @Test
    @Ignore
    public void absoluteRefIsReadSuccessfully() throws ClassNotFoundException, NoSuchMethodException {

        Class<?> absoluteRefClass = generateAndCompile("/schema/ref/absoluteRef.json", "com.example", false).loadClass("com.example.AbsoluteRef");

        Class<?> addressClass = absoluteRefClass.getMethod("getAddress").getReturnType();

        assertThat(addressClass.getName(), is("com.example.Address"));
        assertThat(addressClass.getMethods(), hasItemInArray(hasProperty("name", equalTo("getPostal_code"))));

    }

}