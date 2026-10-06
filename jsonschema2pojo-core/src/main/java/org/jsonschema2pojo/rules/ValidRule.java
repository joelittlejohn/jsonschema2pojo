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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.model.JAnnotatedClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;

import jakarta.validation.Valid;

/**
 * Applies the {@code @Valid} annotation to non-container types that require cascading validation.
 * <p>
 * Container types (Collections, Maps) are not annotated — only their element/value types are,
 * via the recursive rule pipeline.
 */
public class ValidRule implements Rule<JType, JType> {

    private final RuleFactory ruleFactory;

    public ValidRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JType apply(String nodeName, JsonNode node, JsonNode parent, JType type, Schema currentSchema) {
        if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations() && type instanceof JClass jclass) {
            // existingJavaType takes precedence over definition
            if (null != node && node.has("existingJavaType")) {
                final var nodeType = node.at("/type").asText();
                if ("object".equals(nodeType) || "array".equals(nodeType)) {
                    return tryAnnotate(jclass);
                }
            } else {
                return annotateIfApplicable(jclass);
            }
        }
        return type;
    }

    private JClass tryAnnotate(JClass jclass) {
        final JClass erasure = jclass.erasure();
        if (jclass.isReference() && isContainer(erasure)) {
            final var typeParameters = jclass.getTypeParameters();
            if ((jclass.owner().ref(Iterable.class).isAssignableFrom(erasure)
                    || jclass.owner().ref(Optional.class).isAssignableFrom(erasure))
                    && typeParameters.size() == 1
                    && !isScalar(typeParameters.get(0))) {
                return erasure.narrow(tryAnnotate(typeParameters.get(0)));
            } else if (jclass.owner().ref(Map.class).isAssignableFrom(erasure)
                    && typeParameters.size() == 2 && !isScalar(typeParameters.get(1))) {
                return erasure.narrow(typeParameters.get(0), tryAnnotate(typeParameters.get(1)));
            }
        }
        return annotateIfApplicable(jclass);
    }

    private JClass annotateIfApplicable(JClass jclass) {
        if (!isContainer(jclass) && !isScalar(jclass)) {
            return JAnnotatedClass.of(jclass).annotated(getValidClass());
        }
        return jclass;
    }

    private boolean isContainer(JClass jclass) {
        JClass e = jclass.erasure();
        return jclass.owner().ref(Collection.class).isAssignableFrom(e)
            || jclass.owner().ref(Map.class).isAssignableFrom(e)
            || jclass.owner().ref(Optional.class).isAssignableFrom(e);
    }

    // Scalar types that never benefit from cascading @Valid validation.
    // java.lang.Object is intentionally excluded — at runtime it may hold
    // a complex type (e.g. additionalProperties values) that should be validated.
    protected boolean isScalar(JClass jclass) {
        String name = jclass.erasure().fullName();
        return (name.startsWith("java.lang.") && !name.equals("java.lang.Object"))
            || name.startsWith("java.math.");
    }

    private Class<? extends Annotation> getValidClass() {
        return ruleFactory.getGenerationConfig().isUseJakartaValidation()
                ? Valid.class
                : javax.validation.Valid.class;
    }

}
