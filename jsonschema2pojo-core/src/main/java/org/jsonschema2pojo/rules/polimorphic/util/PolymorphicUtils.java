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

package org.jsonschema2pojo.rules.polimorphic.util;

import org.joda.time.IllegalFieldValueException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;

/**
 * Utilities for attrs of Jnode
 * 
 * Created by manuel
 */
public final class PolymorphicUtils {

	/** JsonTypeInfo.Id attr name */
	public static final String JSON_ID_ATTR_NAME = "idValueType";

	/** JsonTypeInfo.Include attr name */
	public static final String JSON_INCLUDE_ATTR_NAME = "includeValueType";

	/** Property discriminator name */
	public static final String JSON_DISCRIMINATOR_PROPERTY_ATTR_NAME = "propertyName";

	private PolymorphicUtils() {
	}

	/**
	 * Get value of discriminator type, return default PROPERTY strategy if bas
	 * value assigned
	 * 
	 * @param nodeAnnotation
	 * @return
	 */
	public static JsonTypeInfo.As getIncludeValue(JsonNode nodeAnnotation) {
		JsonTypeInfo.As includeDiscriminatorType = JsonTypeInfo.As.PROPERTY;
		boolean hasIncludeValueType = nodeAnnotation.has(JSON_INCLUDE_ATTR_NAME);
		if (hasIncludeValueType) {
			JsonNode jsonNode = nodeAnnotation.get(JSON_INCLUDE_ATTR_NAME);
			JsonTypeInfo.As nodeValue = JsonTypeInfo.As.valueOf(jsonNode.asText());
			if (includeDiscriminatorType == null) {
				throw new IllegalFieldValueException(JSON_INCLUDE_ATTR_NAME, jsonNode.asText());
			}
			includeDiscriminatorType = nodeValue;
		}

		return includeDiscriminatorType;

	}

	/**
	 * Get value of Id, return default PROPERTY strategy if bas value assigned
	 * 
	 * @param nodeAnnotation
	 * @return
	 */
	public static JsonTypeInfo.Id getIdValue(JsonNode nodeAnnotation) {
		JsonTypeInfo.Id idType = JsonTypeInfo.Id.CLASS;
		boolean hasIncludeValueType = nodeAnnotation.has(JSON_ID_ATTR_NAME);
		if (hasIncludeValueType) {
			JsonNode jsonNode = nodeAnnotation.get(JSON_ID_ATTR_NAME);
			JsonTypeInfo.Id nodeValue = JsonTypeInfo.Id.valueOf(jsonNode.asText());
			if (idType == null) {
				throw new IllegalFieldValueException(JSON_ID_ATTR_NAME, jsonNode.asText());
			}
			idType = nodeValue;
		}

		return idType;

	}

	/**
	 * 
	 * @param jsonTypeInfo
	 * @param node
	 */
	public static JAnnotationUse completeJAnnotationUse(JAnnotationUse jsonTypeInfo, JsonNode node) {
		JsonTypeInfo.Id id = PolymorphicUtils.getIdValue(node);
		JsonTypeInfo.As includeValue = PolymorphicUtils.getIncludeValue(node);
		jsonTypeInfo.param("use", id);
		jsonTypeInfo.param("include", includeValue);
		String asText = node.get(PolymorphicUtils.JSON_DISCRIMINATOR_PROPERTY_ATTR_NAME).asText();
		jsonTypeInfo.param("property", asText);
		jsonTypeInfo.param("visible", true);
		return jsonTypeInfo;
	}

}
