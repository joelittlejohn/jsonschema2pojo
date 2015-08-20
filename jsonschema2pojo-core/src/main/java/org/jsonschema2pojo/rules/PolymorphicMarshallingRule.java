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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;

import java.util.Iterator;

/**
 * Created by idominguez on 20/08/15.
 */
public class PolymorphicMarshallingRule implements Rule<JFieldVar, JFieldVar> {

    private RuleFactory ruleFactory;

    public PolymorphicMarshallingRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JFieldVar field, Schema currentSchema) {


        if (node.has("externalProperty") && node.get("externalProperty").booleanValue()){
            JAnnotationUse jsonTypeInfo = field.annotate(JsonTypeInfo.class);
            jsonTypeInfo.param("use", JsonTypeInfo.Id.NAME);
            jsonTypeInfo.param("include", JsonTypeInfo.As.EXTERNAL_PROPERTY);
            jsonTypeInfo.param("property", node.get("propertyName").asText());
            jsonTypeInfo.param("visible", true);
            JAnnotationUse jsonSubTypes = field.annotate(JsonSubTypes.class);
            JAnnotationArrayMember jsonSubTypesValues = jsonSubTypes.paramArray("value");
            Iterator<JsonNode> subClasses = node.get("children").iterator();

            while(subClasses.hasNext()) {
                JsonNode childAnnotationData = subClasses.next();
                String subClass = childAnnotationData.get("className").asText();
                String value = childAnnotationData.get("value").asText();
                addJsonSubtypeAnnotation(field, jsonSubTypesValues, subClass, value);
            }
        }
        return field;
    }

    private void addJsonSubtypeAnnotation(JFieldVar field, JAnnotationArrayMember jsonSubTypesValues, String subClass, String value) {
        JAnnotationUse jsonSubType = jsonSubTypesValues.annotate(JsonSubTypes.Type.class);
        jsonSubType.param("value",  field.type().owner().ref(subClass));
        jsonSubType.param("name", value);

    }
}
