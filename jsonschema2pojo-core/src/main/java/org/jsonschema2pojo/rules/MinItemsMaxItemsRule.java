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

package org.jsonschema2pojo.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.Size;

public class MinItemsMaxItemsRule extends AbstractRuleFactoryRule<JFieldVar, JFieldVar> {

    protected MinItemsMaxItemsRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()
                && (node.has("minItems") || node.has("maxItems"))
                && isApplicableType(field)) {

            final Class<? extends Annotation> sizeClass
                    = ruleFactory.getGenerationConfig().isUseJakartaValidation()
                    ? Size.class
                    : javax.validation.constraints.Size.class;
            JAnnotationUse annotation = field.annotate(sizeClass);

            if (node.has("minItems")) {
                annotation.param("min", node.get("minItems").asInt());
            }

            if (node.has("maxItems")) {
                annotation.param("max", node.get("maxItems").asInt());
            }
        }

        return field;
    }

    private boolean isApplicableType(JFieldVar field) {
        try {
            String typeName = field.type().boxify().fullName();
            // For collections, the full name will be something like 'java.util.List<String>' and we
            // need just 'java.util.List'.
            int genericsPos = typeName.indexOf('<');
            if (genericsPos > -1) {
                typeName = typeName.substring(0, genericsPos);
            }

            Class<?> fieldClass = Class.forName(typeName);
            return String.class.isAssignableFrom(fieldClass)
                    || Collection.class.isAssignableFrom(fieldClass)
                    || Map.class.isAssignableFrom(fieldClass)
                    || Array.class.isAssignableFrom(fieldClass)
                    || field.type().isArray();
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

}
