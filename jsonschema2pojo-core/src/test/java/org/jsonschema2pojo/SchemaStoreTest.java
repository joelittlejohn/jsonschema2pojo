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

package org.jsonschema2pojo;

import static org.apache.commons.lang3.StringUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import org.mockito.internal.matchers.Same;

public class SchemaStoreTest {

    @Test
    public void createWithAbsolutePath() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema = new SchemaStore().create(schemaUri, "#/.");

        assertThat(schema, is(notNullValue()));
        assertThat(schema.getId(), is(equalTo(schemaUri)));
        assertThat(schema.getContent().has("description"), is(true));
        assertThat(schema.getContent().get("description").asText(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));

    }

    @Test
    public void createWithRelativePath() throws URISyntaxException {

        URI addressSchemaUri = getClass().getResource("/schema/address.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(addressSchemaUri, "#/.");
        Schema enumSchema = schemaStore.create(addressSchema, "enum.json", "#/.");

        String expectedUri = removeEnd(addressSchemaUri.toString(), "address.json") + "enum.json";

        assertThat(enumSchema, is(notNullValue()));
        assertThat(enumSchema.getId(), is(equalTo(URI.create(expectedUri))));
        assertThat(enumSchema.getContent().has("enum"), is(true));

    }

    @Test
    public void createWithRelativeSegmentsInPath() throws URISyntaxException {

        //Get to the directory of the module jsonschema2pojo-core
        Path basePath = Paths.get(getClass().getResource("/schema/person.json").toURI()).getParent().getParent().getParent().getParent();

        //Now load the resource with a relative path segment
        File relativePath = new File(Paths.get(basePath.toString(),"target", "..", "src", "test", "resources", "schema", "person.json").toString());
        //Now load the resource with the same path minus the relative segment
        File nonRelativePath = new File(Paths.get(basePath.toString(), "src", "test", "resources", "schema", "person.json").toString());

        SchemaStore schemaStore = new SchemaStore();

        Schema schemaWithRelativeSegment = schemaStore.create(relativePath.toURI(), "#/.");
        Schema schemaWithoutRelativeSegment = schemaStore.create(nonRelativePath.toURI(), "#/.");

        //Both schema objects should have the same Id value since their URI's point to the same resource
        assertThat(schemaWithoutRelativeSegment.getId(), is(schemaWithRelativeSegment.getId()));
        assertThat(schemaWithoutRelativeSegment, sameInstance(schemaWithRelativeSegment));
    }

    @Test
    public void createWithSelfRef() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(schemaUri, "#/.");
        Schema selfRefSchema = schemaStore.create(addressSchema, "#", "#/.");

        assertThat(addressSchema, is(sameInstance(selfRefSchema)));

    }

    @Test
    public void createWithEmbeddedSelfRef() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/embeddedRef.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema topSchema = schemaStore.create(schemaUri, "#/.");
        Schema embeddedSchema = schemaStore.create(topSchema, "#/definitions/embedded", "#/.");
        Schema selfRefSchema = schemaStore.create(embeddedSchema, "#", "#/.");

        assertThat(topSchema, is(sameInstance(selfRefSchema)));

    }

    @Test
    public void createWithFragmentResolution() throws URISyntaxException {

        URI addressSchemaUri = getClass().getResource("/schema/address.json").toURI();

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(addressSchemaUri, "#/.");
        Schema innerSchema = schemaStore.create(addressSchema, "#/properties/post-office-box", "#/.");

        String expectedUri = addressSchemaUri.toString() + "#/properties/post-office-box";

        assertThat(innerSchema, is(notNullValue()));
        assertThat(innerSchema.getId(), is(equalTo(URI.create(expectedUri))));
        assertThat(innerSchema.getContent().has("type"), is(true));
        assertThat(innerSchema.getContent().get("type").asText(), is("string"));

    }

    @Test
    public void schemaAlreadyReadIsReused() throws URISyntaxException {

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        SchemaStore schemaStore = new SchemaStore();

        Schema schema1 = schemaStore.create(schemaUri, "#/.");

        Schema schema2 = schemaStore.create(schemaUri, "#/.");

        assertThat(schema1, is(sameInstance(schema2)));

    }

    @Test
    public void setIfEmptyOnlySetsIfEmpty() throws URISyntaxException {

        JType firstClass = mock(JDefinedClass.class);
        JType secondClass = mock(JDefinedClass.class);

        URI schemaUri = getClass().getResource("/schema/address.json").toURI();

        Schema schema = new SchemaStore().create(schemaUri, "#/.");

        schema.setJavaTypeIfEmpty(firstClass);
        assertThat(schema.getJavaType(), is(equalTo(firstClass)));

        schema.setJavaTypeIfEmpty(secondClass);
        assertThat(schema.getJavaType(), is(not(equalTo(secondClass))));

    }

}
