/**
 * Copyright © 2010-2017 Nokia
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

package org.jsonschema2pojo.cli;

import org.jsonschema2pojo.RuleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineLogger implements RuleLogger {

  private final Logger log = LoggerFactory.getLogger(Jsonschema2PojoCLI.class);

  @Override
  public void debug(String msg) {
    log.debug(msg);
  }

  @Override
  public void error(String msg) {
    log.error(msg);
  }

  @Override
  public void info(String msg) {
    log.info(msg);
  }

  @Override
  public void trace(String msg) {
    log.trace(msg);
  }

  @Override
  public void warn(String msg) {
    log.warn(msg);
  }
}
