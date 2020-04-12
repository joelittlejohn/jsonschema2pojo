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

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

/**
 * A JSON Schema document.
 */
public class Schema {

    private final URI id;
    private final JsonNode content;
    private final Schema parent;
    private JType javaType;

    public Schema(URI id, JsonNode content, Schema parent) {
        this.id = id;
        this.content = content;
        this.parent = parent != null ? parent : this;
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

    public Schema getParent() {
        return parent;
    }
    
    public boolean isGenerated() {
        return javaType != null;
    }

    /**
     * Derive a schema with {@code content} and this schema as parent.
     * It will keep the same ID as the parent schema.
     * <p>
     * This method is a no-op if {@code content == this.content}.
     *
     * @param content the content of the child schema
     * @return a schema with the provided content; {@code this} schema if content
     *         didn't change
     */
    public Schema deriveChildSchema(JsonNode content) {
        if (content != this.content) {
            return new Schema(id, content, this);
        } else {
            return this;
        }
    }

}
