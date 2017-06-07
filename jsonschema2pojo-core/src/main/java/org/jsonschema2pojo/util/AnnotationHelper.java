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

package org.jsonschema2pojo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationHelper {

	private static void addParameter(Method method, String param, JAnnotationUse jAnnotationUse, JsonNode node) {
		Class returnType = method.getReturnType();

		if (returnType == String.class) {
			jAnnotationUse.param(param, node.asText());

		} else if (returnType == int.class) {
			jAnnotationUse.param(param, node.asInt());

		} else if (returnType == long.class) {
			jAnnotationUse.param(param, node.asLong());
		}
	}

	public static JAnnotationUse annotateField(Class<? extends Annotation> annotation, JsonNode node, JFieldVar field, String optParamName) {
		JAnnotationUse jAnnotationUse = field.annotate(annotation);

	 	if (node != null && node.isObject()) {
			for (Method next : annotation.getDeclaredMethods()) {
				String param = next.getName();
				if (node.has(param)) {
					addParameter(next, param, jAnnotationUse, node.get(param));
				}
			}
		} else if (optParamName != null){
			try {
				addParameter(annotation.getMethod(optParamName), optParamName, jAnnotationUse, node);
			} catch (NoSuchMethodException e) {}

		}
		return jAnnotationUse;
	}
}
