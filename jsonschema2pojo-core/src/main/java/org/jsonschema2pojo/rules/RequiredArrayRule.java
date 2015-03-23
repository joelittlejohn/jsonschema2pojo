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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.Schema;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 * Applies the "required" JSON schema rule.
 *
 * @see <a
 * href="http://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-5.4.3">http://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-5.4.3</a>
 */
public class RequiredArrayRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    public static final String REQUIRED_COMMENT_TEXT = "\n(Required)";

    protected RequiredArrayRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {
        List<String> requiredFieldMethods = new ArrayList<String>();

        for (Iterator iterator = node.elements(); iterator.hasNext(); ) {
            String fieldName = ((JsonNode) iterator.next()).asText();
            JFieldVar field = jclass.fields().get(fieldName);

            if (field == null) {
                continue;
            }

            addJavaDoc(field);

            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
                addNotNullAnnotation(field);
            }

            requiredFieldMethods.add(getGetterName(fieldName, field.type()));
            requiredFieldMethods.add(getSetterName(fieldName));
        }

        updateGetterSetterJavaDoc(jclass, requiredFieldMethods);

        return jclass;
    }

    private void updateGetterSetterJavaDoc(JDefinedClass jclass, List<String> requiredFieldMethods) {
        for (Iterator methods = jclass.methods().iterator(); methods.hasNext(); ) {
            JMethod method = (JMethod) methods.next();
            if (requiredFieldMethods.contains(method.name())) {
                addJavaDoc(method);
            }
        }
    }

    private void addNotNullAnnotation(JFieldVar field) {
        field.annotate(NotNull.class);
    }


    private void addJavaDoc(JDocCommentable docCommentable) {
        JDocComment javadoc = docCommentable.javadoc();
        javadoc.append(REQUIRED_COMMENT_TEXT);
    }

    private String getSetterName(String propertyName) {
        return ruleFactory.getNameHelper().getSetterName(propertyName);
    }

    private String getGetterName(String propertyName, JType type) {
        return ruleFactory.getNameHelper().getGetterName(propertyName, type);
    }

}
