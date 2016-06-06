/**
 * Copyright Â© 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jsonschema2pojo.swagger2;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringUtils;
import org.jsonschema2pojo.rules.RuleFactory;

import java.util.List;

import static org.apache.commons.lang.StringUtils.join;

/**
 * @author activey
 */
public class Swagger2AnnotationApplier {

  private static final String NODE_PROPERTY_DESCRIPTION = "description";
  private static final String NODE_PROPERTY_REQUIRED = "required";
  private static final String ANNOTATION_PARAM_NAME = "name";
  private static final String ANNOTATION_PARAM_VALUE = "value";
  private static final String ANNOTATION_PARAM_REQUIRED = "required";
  private static final String ANNOTATION_PARAM_DESCRIPTION = "description";
  private static final String ANNOTATION_PARAM_ALLOWABLE_VALUES = "allowableValues";

  private final boolean swaggerEnabled;

  public Swagger2AnnotationApplier(RuleFactory ruleFactory) {
    this.swaggerEnabled = ruleFactory.getGenerationConfig().isIncludeSwagger2Annotations();
  }

  public void applyModelAnnotation(JDefinedClass jclass, JsonNode node) {
    if (!swaggerEnabled) {
      return;
    }
    JAnnotationUse annotation = jclass.annotate(ApiModel.class);
    if (node.has(NODE_PROPERTY_DESCRIPTION)) {
      annotation.param(ANNOTATION_PARAM_DESCRIPTION, node.get(NODE_PROPERTY_DESCRIPTION).asText());
    }
  }

  public void applyModelPropertyAnnotation(JFieldVar field, JsonNode node, String propertyName) {
    if (!swaggerEnabled) {
      return;
    }
    JAnnotationUse annotation =
        modelPropertyAnnotation(field).param(ANNOTATION_PARAM_NAME, propertyName);
    if (node.has(NODE_PROPERTY_DESCRIPTION)) {
      annotation.param(ANNOTATION_PARAM_VALUE, node.get(NODE_PROPERTY_DESCRIPTION).asText());
    }
    if (node.has(NODE_PROPERTY_REQUIRED)) {
      annotation.param(ANNOTATION_PARAM_REQUIRED, node.get(NODE_PROPERTY_REQUIRED).asBoolean());
    }
  }

  public void setModelPropertyAnnotationRequired(JFieldVar field, boolean required) {
    if (!swaggerEnabled) {
      return;
    }
    modelPropertyAnnotation(field).param(ANNOTATION_PARAM_REQUIRED, required);
  }

  public void changeModelPropertyAnnotationMaxLength(JFieldVar field, int maxLength) {
    if (!swaggerEnabled) {
      return;
    }
    // modelPropertyAnnotation(field).param("")
  }

  public void changeModelPropertyAnnotationAllowableValues(JFieldVar field,
      List<String> allowableValues) {
    if (!swaggerEnabled) {
      return;
    }
    modelPropertyAnnotation(field).param(ANNOTATION_PARAM_ALLOWABLE_VALUES,
        join(allowableValues, ","));
  }

  private JAnnotationUse modelPropertyAnnotation(JFieldVar field) {
    JClass annotationClass = field.type().owner().ref(ApiModelProperty.class);
    if (annotationClass == null) {
      return field.annotate(ApiModelProperty.class);
    }
    for (JAnnotationUse annotationUse : field.annotations()) {
      if (annotationUse.getAnnotationClass().isAssignableFrom(annotationClass)) {
        return annotationUse;
      }
    }
    return field.annotate(ApiModelProperty.class);
  }
}
