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

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsonschema2pojo.Schema;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

public class MinimumMaximumRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;

    protected MinimumMaximumRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations() && isApplicableType(field)) {

            if (node.has("minimum")) {
                JAnnotationUse annotation = field.annotate(DecimalMin.class);
                annotation.param("value", node.get("minimum").asText());
            }

            if (node.has("maximum")) {
                JAnnotationUse annotation = field.annotate(DecimalMax.class);
                annotation.param("value", node.get("maximum").asText());
            }

        }

        return field;
    }

    private boolean isApplicableType(JFieldVar field) {
        try {
            Class<?> fieldClass = Class.forName(field.type().boxify().fullName());
            // Support Strings and most number types except Double and Float, per docs on DecimalMax/Min annotations
            return String.class.isAssignableFrom(fieldClass) ||
                    (Number.class.isAssignableFrom(fieldClass) &&
                            !Float.class.isAssignableFrom(fieldClass) && !Double.class.isAssignableFrom(fieldClass));
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

}
