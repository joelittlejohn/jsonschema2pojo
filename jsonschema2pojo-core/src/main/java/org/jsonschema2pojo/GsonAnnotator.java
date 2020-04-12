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

/**
 *
 */
package org.jsonschema2pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;

/**
 * Annotates generated Java types using Gson. The annotations used here are most
 * useful when the JSON fields have characters (like underscores) that are
 * poorly suited for beans. By using the {@link SerializedName} annotation, we
 * are able to preserve the original format. Use this in conjunction with
 * {@link GenerationConfig#getPropertyWordDelimiters} to filter out underscores
 * or other unwanted delimiters but still marshal/unmarshal the same content.
 *
 * @see <a
 *      href="https://code.google.com/p/google-gson/">https://code.google.com/p/google-gson/</a>
 */
public class GsonAnnotator extends AbstractAnnotator {

    public GsonAnnotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(SerializedName.class).param("value", propertyName);
        field.annotate(Expose.class);
    }

    @Override
    public void enumConstant(JDefinedClass _enum, JEnumConstant constant, String value) {
        constant.annotate(SerializedName.class).param("value", value);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return false;
    }

}
