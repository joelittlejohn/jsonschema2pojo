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

import java.util.Date;

import org.codehaus.jackson.JsonNode;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class FormatRule implements SchemaRule<JPackage, JType> {

    @Override
    public JType apply(String nodeName, JsonNode node, JPackage generatableType) {

        if (node.getTextValue().equals("date-time")) {
            return generatableType.owner().ref(Date.class);

        } else if (node.getTextValue().equals("utc-millisec")) {
            return generatableType.owner().LONG;

        } else {
            return generatableType.owner().ref(String.class);
        }

    }

}
