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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import org.jsonschema2pojo.Schema;

public class IdRule implements Rule<JDefinedClass, JFieldVar> {

    protected IdRule() {
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When a $id node is found and applied with this rule, the value of
     * the $id is added as a static constant member variable in the class.
     *
     * @param nodeName
     *            the name of the object to which this description applies
     * @param node
     *            the <code>$id</code> schema node
     * @param parent
     *            the parent node
     * @param clazz
     *            the {@link JDefinedClass} where the ID static member variable
     *            should be written.
     * @return the field created to contain the id
     */
    @Override
    public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass clazz, Schema schema) {
        String id = node.asText();

        if (null == id || id.isEmpty()) {
            return null;
        }

        return clazz.field(
                JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
                String.class,
                "SCHEMA_ID",
                JExpr.lit(id));
    }

}
