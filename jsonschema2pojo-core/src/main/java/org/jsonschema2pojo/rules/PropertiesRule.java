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

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsonschema2pojo.Schema;
import com.sun.codemodel.JDefinedClass;

/**
 * Applies the "properties" schema rule.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2</a>
 */
public class PropertiesRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    protected PropertiesRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * For each property present within the properties node, this rule will
     * invoke the 'property' rule provided by the given schema mapper.
     * 
     * @param nodeName
     *            the name of the node for which properties are being added
     * @param node
     *            the properties node, containing property names and their
     *            definition
     * @param jclass
     *            the Java type which will have the given properties added
     * @return the given jclass
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {

        for (Iterator<String> properties = node.fieldNames(); properties.hasNext();) {
            String property = properties.next();

            ruleFactory.getPropertyRule().apply(property, node.get(property), jclass, schema);
        }

        ruleFactory.getAnnotator().propertyOrder(jclass, node);

        return jclass;
    }
}
