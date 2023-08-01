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

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import jakarta.validation.constraints.Digits;

public class DigitsRule extends AbstractRuleFactoryRule<JFieldVar, JFieldVar> {

    protected DigitsRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()
            && node.has("integerDigits") && node.has("fractionalDigits")
            && isApplicableType(field)) {

            final Class<? extends Annotation> digitsClass
                    = ruleFactory.getGenerationConfig().isUseJakartaValidation()
                    ? Digits.class
                    : javax.validation.constraints.Digits.class;
            JAnnotationUse annotation = field.annotate(digitsClass);

            annotation.param("integer", node.get("integerDigits").asInt());
            annotation.param("fraction", node.get("fractionalDigits").asInt());
        }

        return field;
    }

    private boolean isApplicableType(JFieldVar field) {
        try {
            Class<?> fieldClass = Class.forName(field.type().boxify().fullName());
            // Support Strings and most number types except Double and Float, per docs on Digits annotations
            return String.class.isAssignableFrom(fieldClass) ||
                    (Number.class.isAssignableFrom(fieldClass) &&
                            !Float.class.isAssignableFrom(fieldClass) && !Double.class.isAssignableFrom(fieldClass));
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

}
