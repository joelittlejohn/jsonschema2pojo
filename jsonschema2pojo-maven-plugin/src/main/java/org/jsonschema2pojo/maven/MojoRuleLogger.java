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

package org.jsonschema2pojo.maven;

import org.apache.maven.plugin.logging.Log;
import org.jsonschema2pojo.AbstractRuleLogger;

public class MojoRuleLogger extends AbstractRuleLogger {
  private final Log log;

  public MojoRuleLogger(Log log) {
    super();
    this.log = log;
  }

  @Override
  protected void doDebug(String msg) {
    log.debug(msg);
  }

  @Override
  protected void doError(String msg, Throwable e) {
    if(e != null) {
      log.error(msg, e);
    } else {
      log.error(msg);
    }
  }

  @Override
  protected void doInfo(String msg) {
    log.info(msg);
  }

  @Override
  protected void doTrace(String msg) {
    log.debug(msg); // No trace level for Mojo Logger
  }

  @Override
  protected void doWarn(String msg, Throwable e) {
    if(e != null) {
      log.warn(msg, e);
    } else {
      log.warn(msg);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isDebugEnabled(); // No trace level for Mojo Logger
  }

  @Override
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

}
