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

package org.jsonschema2pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * An annotator (implementing the composite pattern) that can be used to compose
 * many annotators together.
 */
public class CompositeAnnotator implements Annotator {

    final Annotator[] annotators;

    /**
     * Create a new composite annotator, made up of a given set of child
     * annotators.
     *
     * @param annotators
     *            The annotators that will be called whenever this annotator is
     *            called. The child annotators provided will called in the order
     *            that they appear in this argument list.
     */
    public CompositeAnnotator(Annotator... annotators) {
        this.annotators = annotators;
    }

    @Override
    public void typeInfo(JDefinedClass clazz, JsonNode node) {
        for (Annotator annotator : annotators) {
            annotator.typeInfo(clazz, node);
        }
    }

    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        for (Annotator annotator : annotators) {
            annotator.propertyOrder(clazz, propertiesNode);
        }
    }

    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        for (Annotator annotator : annotators) {
            annotator.propertyInclusion(clazz, schema);
        }
    }

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        for (Annotator annotator : annotators) {
            annotator.propertyField(field, clazz, propertyName, propertyNode);
        }
    }

    @Override
    public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
        for (Annotator annotator : annotators) {
            annotator.propertyGetter(getter, clazz, propertyName);
        }
    }

    @Override
    public void propertySetter(JMethod setter, JDefinedClass clazz, String propertyName) {
        for (Annotator annotator : annotators) {
            annotator.propertySetter(setter, clazz, propertyName);
        }
    }

    @Override
    public void anyGetter(JMethod getter, JDefinedClass clazz) {
        for (Annotator annotator : annotators) {
            annotator.anyGetter(getter, clazz);
        }
    }

    @Override
    public void anySetter(JMethod setter, JDefinedClass clazz) {
        for (Annotator annotator : annotators) {
            annotator.anySetter(setter, clazz);
        }
    }

    @Override
    public void enumCreatorMethod(JDefinedClass _enum, JMethod creatorMethod) {
        for (Annotator annotator : annotators) {
            annotator.enumCreatorMethod(_enum, creatorMethod);
        }
    }

    @Override
    public void enumValueMethod(JDefinedClass _enum, JMethod valueMethod) {
        for (Annotator annotator : annotators) {
            annotator.enumValueMethod(_enum, valueMethod);
        }
    }

    @Override
    public void enumConstant(JDefinedClass _enum, JEnumConstant constant, String value) {
        for (Annotator annotator : annotators) {
            annotator.enumConstant(_enum, constant, value);
        }
    }

    @Override
    public boolean isAdditionalPropertiesSupported() {
        for (Annotator annotator : annotators) {
            if (!annotator.isAdditionalPropertiesSupported()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void additionalPropertiesField(JFieldVar field, JDefinedClass clazz, String propertyName) {
        for (Annotator annotator : annotators) {
            annotator.additionalPropertiesField(field, clazz, propertyName);
        }
    }

    @Override
    public boolean isPolymorphicDeserializationSupported(JsonNode node) {
        for (Annotator annotator : annotators) {
            if (!annotator.isPolymorphicDeserializationSupported(node)) {
                return false;
            }
        }
        return true;
    }

    @Override
   public void dateTimeField(JFieldVar field, JDefinedClass clazz, JsonNode propertyNode) {
      for (Annotator annotator : annotators) {
            annotator.dateTimeField(field, clazz, propertyNode);
        }
   }

   @Override
   public void dateField(JFieldVar field, JDefinedClass clazz, JsonNode propertyNode) {
      for (Annotator annotator : annotators) {
            annotator.dateField(field, clazz, propertyNode);
        }
   }

    @Override
    public void timeField(JFieldVar field, JDefinedClass clazz, JsonNode propertyNode) {
        for (Annotator annotator : annotators) {
            annotator.timeField(field, clazz, propertyNode);
        }
    }
}
