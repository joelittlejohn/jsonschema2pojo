/**
 * Copyright © 2010 Nokia
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

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonNode;
import org.easymock.Capture;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.rules.ObjectRule;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class SchemaMapperImplTest {

    @Test
    public void factoryMethodsCreateRules() {

        SchemaMapper schemaMapper = new SchemaMapperImpl();

        assertThat(schemaMapper.getArrayRule(), notNullValue());

        assertThat(schemaMapper.getDescriptionRule(), notNullValue());

        assertThat(schemaMapper.getEnumRule(), notNullValue());

        assertThat(schemaMapper.getObjectRule(), notNullValue());

        assertThat(schemaMapper.getOptionalRule(), notNullValue());

        assertThat(schemaMapper.getPropertiesRule(), notNullValue());

        assertThat(schemaMapper.getPropertyRule(), notNullValue());

        assertThat(schemaMapper.getTypeRule(), notNullValue());

    }

    @Test
    public void generateReadsSchemaAsObject() throws IOException {

        final ObjectRule mockObjectRule = createMock(ObjectRule.class);
        SchemaMapper schemaMapper = new SchemaMapperImpl() {
            @Override
            public ObjectRule getObjectRule() {
                return mockObjectRule;
            }
        };

        Capture<JPackage> capturePackage = new Capture<JPackage>();
        Capture<JsonNode> captureNode = new Capture<JsonNode>();

        expect(mockObjectRule.apply(eq("Address"), capture(captureNode), capture(capturePackage))).andReturn(null);

        JCodeModel codeModel = new JCodeModel();

        InputStream schemaContent = this.getClass().getResourceAsStream("/schema/address.json");

        replay(mockObjectRule);

        schemaMapper.generate(codeModel, "Address", "com.example.package", schemaContent);

        verify(mockObjectRule);

        assertThat(capturePackage.hasCaptured(), is(true));
        assertThat(capturePackage.getValue().name(), is("com.example.package"));
        assertThat(captureNode.hasCaptured(), is(true));
        assertThat(captureNode.getValue().get("description").getTextValue(), is("An Address following the convention of http://microformats.org/wiki/hcard"));

    }

}
