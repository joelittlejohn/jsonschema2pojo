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
    private final JsonNode parentContent;
    private JType javaType;

    public Schema(URI id, JsonNode content, JsonNode parentContent) {
        this.id = id;
        this.content = content;
        this.parentContent = parentContent;
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

    public JsonNode getParentContent() {
        return parentContent;
    }
    
    public boolean isGenerated() {
        return javaType != null;
    }

}
