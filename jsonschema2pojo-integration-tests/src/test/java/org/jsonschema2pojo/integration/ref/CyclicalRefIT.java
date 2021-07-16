/**
 * Copyright © 2010-2020 Nokia
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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class CyclicalRefIT {

    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void cyclicalRefsAreReadSuccessfully() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/subdirectory1/refToSubdirectory2.json", "com.example");

        Class class1 = resultsClassLoader.loadClass("com.example.RefToSubdirectory2");
        Class class2 = resultsClassLoader.loadClass("com.example.RefToSubdirectory1");

        Class refToClass2 = class1.getMethod("getRefToOther").getReturnType();
        Class refToClass1 = class2.getMethod("getRefToOther").getReturnType();

        assertThat(refToClass2, is(equalTo(class2)));
        assertThat(refToClass1, is(equalTo(class1)));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void recursiveTreeNodeExampleIsReadCorrectly() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/recursiveTreeNode.json", "com.example");

        Class clazz = resultsClassLoader.loadClass("com.example.RecursiveTreeNode");

        Class<?> childrenType = clazz.getMethod("getChildren").getReturnType();
        assertThat(childrenType.getName(), is("java.util.List"));

        Type childType = ((ParameterizedType)clazz.getMethod("getChildren").getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(childType.getTypeName(), is("com.example.RecursiveTreeNode"));

    }

    @Test
    public void recursiveTreeNodeWithNoParentFileIsReadCorrectly() throws ClassNotFoundException, NoSuchMethodException, IOException {

        JCodeModel codeModel = new JCodeModel();
        String content = IOUtils.toString(getClass().getResourceAsStream("/schema/ref/recursiveTreeNode.json"));
        JsonNode schema = new ObjectMapper().readTree(content);

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));

    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void recursiveEtcdTreeNodeExampleIsReadCorrectly() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = schemaRule.generateAndCompile("/schema/ref/recursiveEtcdTreeNode.json", "com.example");

        Class clazz = resultsClassLoader.loadClass("com.example.RecursiveEtcdTreeNode");

        Class<?> nodeType = clazz.getMethod("getNode").getReturnType();
        assertThat(nodeType.getName(), is("com.example.Node"));

        Class<?> childrenType = nodeType.getMethod("getNodes").getReturnType();
        assertThat(childrenType.getName(), is("java.util.List"));

        Type childType = ((ParameterizedType)nodeType.getMethod("getNodes").getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(childType.getTypeName(), is("com.example.Node"));

    }

    @Test
    public void recursiveEtcdTreeNodeWithNoParentFileIsReadCorrectly() throws ClassNotFoundException, NoSuchMethodException, IOException {

        JCodeModel codeModel = new JCodeModel();
        String content = IOUtils.toString(getClass().getResourceAsStream("/schema/ref/recursiveEtcdTreeNode.json"));
        JsonNode schema = new ObjectMapper().readTree(content);

        JPackage p = codeModel._package("com.example");
        new RuleFactory().getSchemaRule().apply("Example", schema, null, p, new Schema(null, schema, null));

    }

}