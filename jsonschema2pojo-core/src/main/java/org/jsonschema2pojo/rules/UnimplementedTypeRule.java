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

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

public class UnimplementedTypeRule implements Rule<JClassContainer, JType> {

  private String keyword;
  protected RuleFactory ruleFactory;

  public UnimplementedTypeRule(String keyword, RuleFactory ruleFactory) {
    this.keyword = keyword;
    this.ruleFactory = ruleFactory;
  }

  @Override
  public JType apply(String nodeName, JsonNode node, JsonNode parent, JClassContainer jClassContainer, Schema currentSchema) {
    ruleFactory.getLogger().warn(keyword+" is not implemented.");
    return jClassContainer.owner().ref(Object.class);
  }
}
