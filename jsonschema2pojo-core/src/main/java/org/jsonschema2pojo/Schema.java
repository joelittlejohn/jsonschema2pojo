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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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

    public Schema getGrandParent() {
        if (parent == this) {
            return this;
        } else {
            return this.parent.getGrandParent();
        }
    }

    public boolean isGenerated() {
        return javaType != null;
    }

    public String calculateHash() {
        try {
            return Base64.getEncoder()
                    .encodeToString(MessageDigest.getInstance("SHA-256")
                            .digest(content.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 not available, disable de-duplication to avoid", ex);
        }
    }
}
