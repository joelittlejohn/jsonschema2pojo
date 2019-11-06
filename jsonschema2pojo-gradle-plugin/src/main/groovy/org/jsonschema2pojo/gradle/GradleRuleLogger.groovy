/**
 * Copyright © 2010-2017 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jsonschema2pojo.gradle

import org.gradle.api.logging.Logger
import org.jsonschema2pojo.RuleLogger

class GradleRuleLogger implements RuleLogger {

    def Logger

    GradleRuleLogger(Logger logger) {
        super()
        logger.info("Initializing {}", GradleRuleLogger.class)
        this.logger = logger
    }

    @Override
    void debug(String msg) {
        logger.debug(msg)
    }

    @Override
    void error(String msg) {
        logger.error(msg)
    }

    @Override
    void info(String msg) {
        logger.info(msg)
    }

    @Override
    void trace(String msg) {
        logger.trace(msg)
    }

    @Override
    void warn(String msg) {
        logger.warn(msg)
    }
}
