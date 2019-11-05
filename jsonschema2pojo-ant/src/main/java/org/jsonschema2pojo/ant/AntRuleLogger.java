/*
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

package org.jsonschema2pojo.ant;

import org.apache.tools.ant.Project;
import org.jsonschema2pojo.RuleLogger;

public class AntRuleLogger implements RuleLogger {

  private final Jsonschema2PojoTask task;

  public AntRuleLogger(Jsonschema2PojoTask jsonschema2PojoTask) {
    this.task = jsonschema2PojoTask;
  }

  @Override
  public void debug(String msg) {
    log(msg, Project.MSG_DEBUG);
  }

  @Override
  public void error(String msg) {
    log(msg, Project.MSG_ERR);
  }

  @Override
  public void info(String msg) {
    log(msg, Project.MSG_INFO);
  }

  @Override
  public void trace(String msg) {
    log(msg, Project.MSG_VERBOSE);
  }

  @Override
  public void warn(String msg) {
    log(msg, Project.MSG_WARN);
  }

  private void log(String msg, int level) {
    if (task.getProject() != null) {
      task.getProject().log(msg, level);
    } else {
      switch (level) {
        case Project.MSG_ERR:
          System.err.println("ERROR: " + msg);
          break;
        case Project.MSG_WARN:
          System.err.println("WARNING: " + msg);
          break;
        case Project.MSG_INFO:
          System.err.println("INFO: " + msg);
          break;
        case Project.MSG_DEBUG:
          System.err.println("DEBUG: " + msg);
          break;
        case Project.MSG_VERBOSE:
          System.err.println("VERBOSE: " + msg);
          break;
        default:
          System.err.println(msg);
          break;
      }
    }
  }
}
