/**
 * Copyright © 2016 Tobias Preuss
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

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.moshi.Json;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;

/**
 * Annotates generated Java types using Moshi. The annotations used here are most
 * useful when the JSON fields have characters (like underscores) that are
 * poorly suited for beans. By using the {@link Json} annotation, we
 * are able to preserve the original format.
 *
 * @see <a
 *      href="https://github.com/square/moshi#custom-field-names-with-json">https://github.com/square/moshi#custom-field-names-with-json</a>
 */
public class Moshi1Annotator extends AbstractAnnotator {

    public Moshi1Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(Json.class).param("name", propertyName);
    }

    @Override
    public void enumConstant(JEnumConstant constant, String value) {
        constant.annotate(Json.class).param("name", value);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return false;
    }

}
