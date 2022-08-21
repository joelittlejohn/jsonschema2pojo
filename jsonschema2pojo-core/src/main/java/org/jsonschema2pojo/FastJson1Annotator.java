/**
 * Copyright © 2010-2020 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import com.alibaba.fastjson.annotation.JSONField;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;

/**
 * Annotates generated Java types using FastJson. The annotations used here are most
 * useful when the JSON fields have characters (like underscores) that are
 * poorly suited for beans. By using the {@link JSONField} annotation, we
 * are able to preserve the original format.
 *
 * @see <a
 *      href="https://github.com/alibaba/fastjson">https://github.com/alibaba/fastjson</a>
 */
public class FastJson1Annotator extends AbstractAnnotator {

    public FastJson1Annotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        field.annotate(JSONField.class).param("value", propertyName);
    }

    @Override
    public void enumConstant(JDefinedClass _enum, JEnumConstant constant, String value) {
        constant.annotate(JSONField.class).param("value", value);
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        return false;
    }

}
