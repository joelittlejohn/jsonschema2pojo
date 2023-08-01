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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import jakarta.validation.constraints.NotNull;

/**
 * Applies the "required" JSON schema rule.
 *
 * @see <a
 * href="http://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-5.4.3">http://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-5.4.3</a>
 */
public class RequiredArrayRule extends AbstractRuleFactoryRule<JDefinedClass, JDefinedClass> {

    public static final String REQUIRED_COMMENT_TEXT = "\n(Required)";

    protected RequiredArrayRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
        List<String> requiredFieldMethods = new ArrayList<>();

        JsonNode properties = schema.getContent().get("properties");

        for (Iterator<JsonNode> iterator = node.elements(); iterator.hasNext(); ) {
            String requiredArrayItem = iterator.next().asText();
            if (requiredArrayItem.isEmpty()) {
                continue;
            }

            JsonNode propertyNode = null;

            if (properties != null) {
                propertyNode = properties.findValue(requiredArrayItem);
            }

            String fieldName = ruleFactory.getNameHelper().getPropertyName(requiredArrayItem, propertyNode);
            JFieldVar field = jclass.fields().get(fieldName);

            if (field == null) {
                continue;
            }

            addJavaDoc(field);

            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
                addNotNullAnnotation(field);
            }

            if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()) {
                addNonnullAnnotation(field);
            }

            requiredFieldMethods.add(getGetterName(fieldName, field.type(), node));
            requiredFieldMethods.add(getSetterName(fieldName, node));
        }

        updateGetterSetterJavaDoc(jclass, requiredFieldMethods);

        return jclass;
    }

    private void updateGetterSetterJavaDoc(JDefinedClass jclass, List<String> requiredFieldMethods) {
        for (Iterator<JMethod> methods = jclass.methods().iterator(); methods.hasNext();) {
            JMethod method = methods.next();
            if (requiredFieldMethods.contains(method.name())) {
                addJavaDoc(method);
            }
        }
    }

    private void addNotNullAnnotation(JFieldVar field) {
        final Class<? extends Annotation> notNullClass
                = ruleFactory.getGenerationConfig().isUseJakartaValidation()
                ? NotNull.class
                : javax.validation.constraints.NotNull.class;
        field.annotate(notNullClass);
    }

    private void addNonnullAnnotation(JFieldVar field) {
        field.annotate(Nonnull.class);
    }

    private void addJavaDoc(JDocCommentable docCommentable) {
        JDocComment javadoc = docCommentable.javadoc();
        javadoc.append(REQUIRED_COMMENT_TEXT);
    }

    private String getSetterName(String propertyName, JsonNode node) {
        return ruleFactory.getNameHelper().getSetterName(propertyName, node);
    }

    private String getGetterName(String propertyName, JType type, JsonNode node) {
        return ruleFactory.getNameHelper().getGetterName(propertyName, type, node);
    }

}
