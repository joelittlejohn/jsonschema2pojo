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

public abstract class AbstractTypeInfoAwareAnnotator extends AbstractAnnotator
{
	public AbstractTypeInfoAwareAnnotator(GenerationConfig generationConfig) {
		super(generationConfig);
	}

	@Override
	public void typeInfo(JDefinedClass clazz, JsonNode node) {
		if(getGenerationConfig().isIncludeTypeInfo()) {
			// Have per-schema JavaTypeInfo configuration override what is defined in generation config; backward comparability
			if (node.has("deserializationClassProperty")) {
				String annotationName = node.get("deserializationClassProperty").asText();
				addJsonTypeInfoAnnotation(clazz, annotationName);
			} else {
				addJsonTypeInfoAnnotation(clazz, "@class");
			}
		} else {
			// per-schema JsonTypeInfo configuration
			if (node.has("deserializationClassProperty")) {
				String annotationName = node.get("deserializationClassProperty").asText();
				addJsonTypeInfoAnnotation(clazz, annotationName);
			}
		}
	}

	@Override
	public boolean isPolymorphicDeserializationSupported(JsonNode node) {
		return getGenerationConfig().isIncludeTypeInfo() || node.has("deserializationClassProperty");
	}

	abstract protected void addJsonTypeInfoAnnotation(JDefinedClass clazz, String propertyName);
}
