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

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;

/**
 * Annotates generated Java types using the Jackson 2.x mapping annotations.
 *
 * @see <a
 * href="https://github.com/FasterXML/jackson-annotations">https://github.com/FasterXML/jackson-annotations</a>
 */
public class Jackson2Annotator extends JacksonAnnotator {

    public Jackson2Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    protected Class<? extends Annotation> getJsonDeserializeAnnotation() {
        return com.fasterxml.jackson.databind.annotation.JsonDeserialize.class;
    }
    
    @Override
    public void typeDocumentation(JDefinedClass clazz, JsonNode schema) {
        if (schema.has("description")) {
            clazz.annotate(JsonClassDescription.class).param("value", schema.get("description").asText());
        }
    }
}
