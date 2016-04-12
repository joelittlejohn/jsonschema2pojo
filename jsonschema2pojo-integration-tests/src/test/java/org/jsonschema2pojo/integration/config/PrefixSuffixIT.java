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

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;

import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.junit.Rule;
import org.junit.Test;

public class PrefixSuffixIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    public void defaultClassPrefix() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");
        resultsClassLoader.loadClass("com.example.PrimitiveProperties");
    }

    @Test
    public void customClassPrefix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.AbstractPrimitiveProperties");
    }
    
    @Test
    public void noCapsCustomClassPrefix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNamePrefix","abstract"));
        resultsClassLoader.loadClass("com.example.abstractPrimitiveProperties");
    }

    @Test
    public void defaultClassSuffix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example");
        resultsClassLoader.loadClass("com.example.PrimitiveProperties");
    }

    @Test
    public void customClassSuffix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.PrimitivePropertiesDao");
    }
    
    @Test
    public void noCapsCustomClassSuffix() throws ClassNotFoundException{
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNameSuffix","dao"));
        resultsClassLoader.loadClass("com.example.PrimitivePropertiesdao");
    }

    @Test(expected = ClassNotFoundException.class)
    public void NotExistingClassPrefix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }

    @Test(expected = ClassNotFoundException.class)
    public void NotExistingClassSufix() throws ClassNotFoundException{

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "com.example", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }

    @Test(expected = ClassNotFoundException.class)
    public void SuffixWithDefaultPackageName() throws ClassNotFoundException{
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "", config("classNameSuffix","Dao"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }

    @Test(expected = ClassNotFoundException.class)
    public void PrefixWithDefaultPackageName() throws ClassNotFoundException{
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitiveProperties.json", "", config("classNamePrefix","Abstract"));
        resultsClassLoader.loadClass("com.example.NotExistingPrimitiveProperties");
    }

    @Test
    public void customClassPrefixNoJavaType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitivePropertiesNoJavaType.json",
                "com.example", config("classNamePrefix","Prefix"));
        resultsClassLoader.loadClass("com.example.PrefixPrimitivePropertiesNoJavaType");
    }
    
    @Test
    public void customClassPrefixNoCapsNoJavaType() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitivePropertiesNoJavaType.json",
                "com.example", config("classNamePrefix","prefix"));
        resultsClassLoader.loadClass("com.example.prefixPrimitivePropertiesNoJavaType");
    }

    @Test
    public void customClassSuffixNoJavaType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitivePropertiesNoJavaType.json",
                "com.example", config("classNameSuffix","Suffix"));
        resultsClassLoader.loadClass("com.example.PrimitivePropertiesNoJavaTypeSuffix");
    }
    
    @Test
    public void customClassSuffixNoCapsNoJavaType() throws ClassNotFoundException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitivePropertiesNoJavaType.json",
                "com.example", config("classNameSuffix","suffix"));
        resultsClassLoader.loadClass("com.example.PrimitivePropertiesNoJavaTypesuffix");
    }

    @Test
    public void customClassPrefixAndSuffixNoJavaType() throws ClassNotFoundException {
        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/properties/primitivePropertiesNoJavaType.json",
                "com.example", config("classNamePrefix", "Prefix", "classNameSuffix","Suffix"));
        resultsClassLoader.loadClass("com.example.PrefixPrimitivePropertiesNoJavaTypeSuffix");
    }
}
