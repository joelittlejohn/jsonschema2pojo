/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo;

import java.io.IOException;
import java.net.URL;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.googlecode.jsonschema2pojo.rules.RuleFactory;
import com.googlecode.jsonschema2pojo.rules.RuleFactoryImpl;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

/**
 * Default implementation of the {@link SchemaMapper} interface, accepting a
 * factory which will be used to create type generation rules for this mapper.
 */
public class SchemaMapperImpl implements SchemaMapper {

    private final RuleFactory ruleFactory;

    /**
     * Create a schema mapper with the given {@link RuleFactory}.
     * 
     * @param ruleFactory
     *            A factory used by this mapper to create Java type generation
     *            rules.
     */
    public SchemaMapperImpl(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }
    
    /**
     * Create a schema mapper with the default {@link RuleFactory} implementation.
     * 
     * @see RuleFactoryImpl
     */
    public SchemaMapperImpl() {
        this(new RuleFactoryImpl());
    }

    @Override
    public void generate(JCodeModel codeModel, String className, String packageName, URL schemaUrl) throws IOException {

        JPackage jpackage = codeModel._package(packageName);

        ObjectNode schemaNode = new ObjectMapper().createObjectNode();
        schemaNode.put("$ref", schemaUrl.toString());

        ruleFactory.getSchemaRule().apply(className, schemaNode, jpackage, null);

    }

}
