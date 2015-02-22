/**
 * Copyright © 2010-2014 Nokia
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

package org.jsonschema2pojo.integration.config;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

import org.jsonschema2pojo.Annotator;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

public class PrefixSuffixIT {

    @Test
    public void defaultClassPrefix() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");
        resultsClassLoader.loadClass("com.example.PrimitiveProperties");
    }
    
    @Test
    public void customClassPrefix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.AbstractPrimitiveProperties");
    }
    
    @Test
    public void defaultClassSufix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");
        resultsClassLoader.loadClass("com.example.PrimitiveProperties");
    }
    
    @Test
    public void customClassSuffix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.PrimitivePropertiesDao");
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void NotExitstingClassPrefix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void NotExitstingClassSufix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void SuffixWithDefaultPackageName() throws ClassNotFoundException{
        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void PrefixWithDefaultPackageName() throws ClassNotFoundException{
        ClassLoader resultsClassLoader = generateAndCompile("/schema/properties/primitiveProperties.json", "", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }

    
}
