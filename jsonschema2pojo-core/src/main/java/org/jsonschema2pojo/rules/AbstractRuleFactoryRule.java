/**
 * Copyright © 2010-2020 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.RuleLogger;

public abstract class AbstractRuleFactoryRule<T, R>
		implements Rule<T, R>
{
	protected final RuleFactory ruleFactory;

	public AbstractRuleFactoryRule(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	public GenerationConfig getGenerationConfig() {
		if (ruleFactory == null) {
			throw new RuntimeException("Rule does not have rule factory; unable to get generation config.");
		}

		return ruleFactory.getGenerationConfig();
	}

	public RuleLogger getLogger() {
		if (ruleFactory == null) {
			throw new RuntimeException("Rule does not have rule factory; unable to get rule logger.");
		}

		return ruleFactory.getLogger();
	}

	public RuleFactory getRuleFactory() {
		return ruleFactory;
	}
}
