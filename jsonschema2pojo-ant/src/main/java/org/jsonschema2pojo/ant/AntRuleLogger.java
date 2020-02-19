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

package org.jsonschema2pojo.ant;

import org.apache.tools.ant.Project;
import org.jsonschema2pojo.AbstractRuleLogger;

public class AntRuleLogger extends AbstractRuleLogger {

  private static final String LEVEL_PREFIX = "[";
  private static final String LEVEL_SUFFIX = "] ";
  private static final String DEBUG_LEVEL_PREFIX = LEVEL_PREFIX + "DEBUG" + LEVEL_SUFFIX;
  private static final String ERROR_LEVEL_PREFIX = LEVEL_PREFIX + "ERROR" + LEVEL_SUFFIX;
  private static final String INFO_LEVEL_PREFIX = LEVEL_PREFIX + "INFO" + LEVEL_SUFFIX;
  private static final String TRACE_LEVEL_PREFIX = LEVEL_PREFIX + "TRACE" + LEVEL_SUFFIX;
  private static final String WARN_LEVEL_PREFIX = LEVEL_PREFIX + "WARN" + LEVEL_SUFFIX;

  private final Jsonschema2PojoTask task;

  public AntRuleLogger(Jsonschema2PojoTask jsonschema2PojoTask) {
    this.task = jsonschema2PojoTask;
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  protected void doDebug(String msg) {
    log(msg, null, Project.MSG_DEBUG, DEBUG_LEVEL_PREFIX);
  }

  protected void doError(String msg, Throwable e) {
    log(msg, e, Project.MSG_ERR, ERROR_LEVEL_PREFIX);
  }

  protected void doInfo(String msg) {
    log(msg, null, Project.MSG_INFO, INFO_LEVEL_PREFIX);
  }

  protected void doTrace(String msg) {
    log(msg, null, Project.MSG_VERBOSE, TRACE_LEVEL_PREFIX);
  }

  protected void doWarn(String msg, Throwable e) {
    log(msg, null, Project.MSG_WARN, WARN_LEVEL_PREFIX);
  }

  private void log(String msg, Throwable e, int level, String levelPrefix) {
    if (task != null && task.getProject() != null) {
      if(e != null) {
        task.getProject().log(msg, e, level);
      } else {
        task.getProject().log(msg, level);
      }
    } else {
      System.err.println(levelPrefix + msg);
      if(e != null) {
        e.printStackTrace(System.err);
      }
    }
  }
}
