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

import static org.apache.commons.lang.StringUtils.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
public class SchemaTest {

    @Before 
    public void clearSchemaCache() {
        Schema.clearCache();
    }
    
    @Test
    public void createWithAbsolutePath() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema = Schema.create(schemaUri);

        assertThat(schema, is(notNullValue()));
        assertThat(schema.getId(), is(equalTo(schemaUri)));
        assertThat(schema.getContent().has("description"), is(true));
        assertThat(schema.getContent().get("description").getTextValue(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));

    }

    @Test
    public void createWithRelativePath() throws URISyntaxException {

        URI addressSchemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema addressSchema = Schema.create(addressSchemaUri);
        Schema enumSchema = Schema.create(addressSchema, "enum.json");

        String expectedUri = removeEnd(addressSchemaUri.toString(), "address.json") + "enum.json";

        assertThat(enumSchema, is(notNullValue()));
        assertThat(enumSchema.getId(), is(equalTo(URI.create(expectedUri))));
        assertThat(enumSchema.getContent().has("enum"), is(true));

    }

    @Test
    public void createWithSelfRef() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema addressSchema = Schema.create(schemaUri);
        Schema selfRefSchema = Schema.create(addressSchema, "#");

        assertThat(addressSchema, is(sameInstance(selfRefSchema)));

    }

    @Test
    public void createWithFragmentResolution() throws URISyntaxException {

        URI addressSchemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema addressSchema = Schema.create(addressSchemaUri);
        Schema innerSchema = Schema.create(addressSchema, "#/properties/post-office-box");

        String expectedUri = addressSchemaUri.toString() + "#/properties/post-office-box";

        assertThat(innerSchema, is(notNullValue()));
        assertThat(innerSchema.getId(), is(equalTo(URI.create(expectedUri))));
        assertThat(innerSchema.getContent().has("type"), is(true));
        assertThat(innerSchema.getContent().get("type").getTextValue(), is("string"));

    }

    @Test
    public void schemaAlreadyReadIsReused() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema1 = Schema.create(schemaUri);

        Schema schema2 = Schema.create(schemaUri);

        assertThat(schema1, is(sameInstance(schema2)));

    }
    
    @Test
    public void setIfEmptyOnlySetsIfEmpty() throws URISyntaxException {
        
        JType firstClass = createMock(JDefinedClass.class);
        JType secondClass = createMock(JDefinedClass.class);
        
        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema = Schema.create(schemaUri);
        
        schema.setJavaTypeIfEmpty(firstClass);
        assertThat(schema.getJavaType(), is(equalTo(firstClass)));
        
        schema.setJavaTypeIfEmpty(secondClass);
        assertThat(schema.getJavaType(), is(not(equalTo(secondClass))));
        
    }

}
