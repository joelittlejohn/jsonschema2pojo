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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.codemodel.*;
import org.jsonschema2pojo.Schema;

import java.util.Iterator;

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
        ruleFactory.getNameHelper().pushToNodeContext(nodeName);

        if (node == null) {
            node = JsonNodeFactory.instance.objectNode();
        }

        for (Iterator<String> properties = node.fieldNames(); properties.hasNext(); ) {
            String property = properties.next();

            ruleFactory.getPropertyRule().apply(property, node.get(property), jclass, schema);
        }

        if (ruleFactory.getGenerationConfig().isGenerateBuilders()) {
            if (!jclass._extends().name().equals("Object")) {
                addOverrideBuilders(jclass, jclass.owner()._getClass(jclass._extends().fullName()));
            }
        }

        ruleFactory.getAnnotator().propertyOrder(jclass, node);
        ruleFactory.getNameHelper().popFromNodeContext();
        return jclass;
    }

    private void addOverrideBuilders(JDefinedClass jclass, JDefinedClass parentJclass) {
        if (parentJclass == null) {
            return;
        }

        for (JMethod parentJMethod : parentJclass.methods()) {
            if (parentJMethod.name().startsWith("with") && parentJMethod.params().size() == 1) {
                addOverrideBuilder(jclass, parentJMethod, parentJMethod.params().get(0));
            }
        }
    }

    private void addOverrideBuilder(JDefinedClass thisJDefinedClass, JMethod parentBuilder, JVar parentParam) {
        
        if (thisJDefinedClass.getMethod(parentBuilder.name(), new JType[] {parentParam.type()}) == null) {
        
            JMethod builder = thisJDefinedClass.method(parentBuilder.mods().getValue(), thisJDefinedClass, parentBuilder.name());
            builder.annotate(Override.class);
    
            JVar param = builder.param(parentParam.type(), parentParam.name());
            JBlock body = builder.body();
            body.invoke(JExpr._super(), parentBuilder).arg(param);
            body._return(JExpr._this());
    
        }
    }

}
