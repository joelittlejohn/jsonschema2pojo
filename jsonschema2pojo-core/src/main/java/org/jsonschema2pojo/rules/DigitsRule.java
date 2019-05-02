/**
 * Copyright © 2010-2017 Nokia
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

package org.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;

import javax.validation.constraints.Digits;
import java.util.NoSuchElementException;

public class DigitsRule implements Rule<JFieldVar, JFieldVar> {

    private final RuleFactory ruleFactory;

    protected DigitsRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema currentSchema) {

        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()
            && node.has("digits")) {

            JAnnotationUse annotation = field.annotate(Digits.class);

            if (node.get("digits").get("integerDigits") == null ||
                node.get("digits").get("fractionalDigits") == null) {
                throw new NoSuchElementException("Cannot find both 'integerDigits' and 'fractionalDigits' declared within 'digits' constraint." );
            }

            annotation.param("integer", node.get("digits").get("integerDigits").asInt());
            annotation.param("fraction", node.get("digits").get("fractionalDigits").asInt());
        }

        return field;
    }

}
