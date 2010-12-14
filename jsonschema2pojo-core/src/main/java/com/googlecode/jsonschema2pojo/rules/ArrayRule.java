/**
 * Copyright © 2010 Nokia
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

package com.googlecode.jsonschema2pojo.rules;

import static org.apache.commons.lang.StringUtils.*;

import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

/**
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.3">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.3</a>
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.13">http://tools.ietf.org/html/draft-zyp-json-schema-02#section-5.13</a>
 */
public class ArrayRule implements SchemaRule<JDefinedClass, JClass> {

    private final SchemaMapper mapper;

    public ArrayRule(SchemaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JClass apply(String nodeName, JsonNode node, JDefinedClass generatableType) {

        boolean uniqueItems = (node.get("uniqueItems") != null) && node.get("uniqueItems").getBooleanValue();

        JType itemType = mapper.getTypeRule().apply(makeSingular(nodeName), node.get("items"), generatableType);

        if (uniqueItems) {
            return generatableType.owner().ref(Set.class).narrow(itemType);
        } else {
            return generatableType.owner().ref(List.class).narrow(itemType);
        }
    }

    private String makeSingular(String nodeName) {
        return removeEnd(removeEnd(nodeName, "s"), "S");
    }

}
