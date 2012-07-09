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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JType;

/**
 * A JSON Schema document.
 */
public class Schema {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static Map<URI, Schema> schemas = new HashMap<URI, Schema>();

    private static FragmentResolver fragmentResolver = new FragmentResolverImpl();

    private URI id;
    private JsonNode content;
    private JType javaType;

    private Schema(URI id, JsonNode content) {
        this.id = id;
        this.content = content;
    }

    /**
     * Create or look up a new schema which has the given ID and read the
     * contents of the given ID as a URL. If a schema with the given ID is
     * already known, then a reference to the original schema will be returned.
     * 
     * @param id
     *            the id of the schema being created
     * @return a schema object containing the contents of the given path
     */
    public static synchronized Schema create(URI id) {

        if (!schemas.containsKey(id)) {

            try {
                JsonNode content = OBJECT_MAPPER.readTree(new File(removeFragment(id)));

                if (id.toString().contains("#")) {
                    content = fragmentResolver.resolve(content, '#' + substringAfter(id.toString(), "#"));
                }

                schemas.put(id, new Schema(id, content));
            } catch (IOException e) {
                String msg = "Error with schema: " + id;
                throw new GenerationException(msg, e);
            }
        }

        return schemas.get(id);
    }

    private static URI removeFragment(URI id) {
        return URI.create(substringBefore(id.toString(), "#"));
    }

    /**
     * Create or look up a new schema using the given schema as a parent and the
     * path as a relative reference. If a schema with the given parent and
     * relative path is already known, then a reference to the original schema
     * will be returned.
     * 
     * @param parent
     *            the schema which is the parent of the schema to be created.
     * @param path
     *            the relative path of this schema (will be used to create a
     *            complete URI by resolving this path against the parent
     *            schema's id)
     * @return a schema object containing the contents of the given path
     */
    public static Schema create(Schema parent, String path) {

        if (path.equals("#")) {
            return parent;
        }

        path = stripEnd(path, "#?&/");

        URI id = (parent == null) ? URI.create(path) : parent.getId().resolve(path);

        return create(id);

    }

    public JType getJavaType() {
        return javaType;
    }

    public void setJavaType(JType javaType) {
        this.javaType = javaType;
    }

    public void setJavaTypeIfEmpty(JType javaType) {
        if (this.getJavaType() == null) {
            this.setJavaType(javaType);
        }
    }

    public URI getId() {
        return id;
    }

    public JsonNode getContent() {
        return content;
    }

    public boolean isGenerated() {
        return (javaType != null);
    }

    public static synchronized void clearCache() {
        schemas.clear();
    }

}
