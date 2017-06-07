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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnnotationHelperTest {


	private JFieldVar jFieldVar;
	private Random random;

	@Before
	public void setup() throws Exception {
		JDefinedClass jDefinedClass = new JCodeModel()._class(this.getClass().getName());
		jFieldVar = jDefinedClass.field(JMod.PRIVATE, jDefinedClass.owner().ref(String.class), "fooBar");

		random = new Random();
	}

	private void assertMatches(Class<? extends Annotation> clazz, Map<String, ?> params, JAnnotationUse jAnnotationUse) {
		StringWriter actualSw = new StringWriter();
		JFormatter formatter = new JFormatter(actualSw);
		jAnnotationUse.generate(formatter);

		String actual = actualSw.toString();

		if (params.isEmpty()) {
			assertEquals(String.format("@%s", clazz.getName()), actual);
			return;
		}

		for (String key : params.keySet()) {
			StringWriter expectedSw = new StringWriter();
			Object value = params.get(key);

			if (!StringUtils.equals(key, "value")) {
				expectedSw.append(key);
				expectedSw.append(" = ");
			}

			if (value instanceof String) {
				expectedSw.append('"');
				expectedSw.append((String)value);
				expectedSw.append('"');

			} else {
				expectedSw.write(value.toString());

				if (value instanceof Long || value.getClass() == long.class) {
					expectedSw.append('L');
				}
			}

			String expected = expectedSw.toString();

			assertThat("Expecting " + expected, actual, containsString(expected));
		}

	}

	private void testClass(Class<? extends Annotation> clazz, Map<String, ?> expected, Object params, String optParamName) {
		Collection<JAnnotationUse> annotations = jFieldVar.annotations();
		assertThat(annotations.size(), is(0));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualParams = mapper.valueToTree(params);

		AnnotationHelper.annotateField(clazz, actualParams, jFieldVar, optParamName);

		annotations = jFieldVar.annotations();

		assertNotNull(annotations);
		assertThat(annotations.size(), is(1));
		for (JAnnotationUse next : annotations) {
			assertMatches(clazz, expected, next);
		}
	}

	@Test
	public void noParameters() {

		Class<? extends Annotation> clazz = NotNull.class;
		testClass(clazz, new HashMap<String, String>(), null, null);
	}

	@Test
	public void applySingleParameter() {

		Class<? extends Annotation> clazz = Pattern.class;

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("regexp", "regexp");

		testClass(clazz, expected, "regexp", "regexp");
	}

	@Test
	public void applyMultipleParameters() {

		Class<? extends Annotation> clazz = Pattern.class;

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("regexp", UUID.randomUUID().toString());
		expected.put("message", UUID.randomUUID().toString());

		testClass(clazz, expected, expected, null);
	}


	@Test
	public void ignoresExtraParams() {

		Class<? extends Annotation> clazz = Pattern.class;

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("regexp", UUID.randomUUID().toString());
		expected.put("message", UUID.randomUUID().toString());

		Map<String, Object> extras = new HashMap<String, Object>(expected);

		extras.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());

		testClass(clazz, expected, extras, null);
	}

	@Test
	public void testStringParameters() {

		Map<String, String> params = new HashMap<String, String>();
		params.put("regexp", UUID.randomUUID().toString());
		params.put("message", UUID.randomUUID().toString());
		testClass(Pattern.class, params, params, null);
	}

	@Test
	public void testIntParameters() {

		Map<String, Integer> params = new HashMap<String, Integer>();
		params.put("integer", random.nextInt());
		params.put("fraction", random.nextInt());
		testClass(Digits.class, params, params, null);
	}

	@Test
	public void testLongParameters() {

		Map<String, Long> params = new HashMap<String, Long>();
		params.put("value", random.nextLong());
		testClass(Max.class, params, params, null);
	}

}
