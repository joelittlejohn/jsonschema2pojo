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

package org.jsonschema2pojo.integration.util;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import static org.jsonschema2pojo.integration.util.LogEvent.Level.DEBUG;
import static org.jsonschema2pojo.integration.util.LogEvent.Level.INFO;
import static org.jsonschema2pojo.integration.util.LogEvent.Level.WARN;
import static org.jsonschema2pojo.integration.util.LogEvent.Level.ERROR;

public class CapturingLogger implements Log {

  List<LogEvent> logs;

  public CapturingLogger(List<LogEvent> logs) {
    this.logs = logs;
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public void debug(CharSequence content) {
    logs.add(new LogEvent(DEBUG, content.toString(), null));
  }

  @Override
  public void debug(CharSequence content, Throwable error) {
    logs.add(new LogEvent(DEBUG, content.toString(), error));
  }

  @Override
  public void debug(Throwable error) {
    logs.add(new LogEvent(DEBUG, null, error));
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public void info(CharSequence content) {
    logs.add(new LogEvent(INFO, content.toString(), null));
  }

  @Override
  public void info(CharSequence content, Throwable error) {
    logs.add(new LogEvent(INFO, content.toString(), error));
  }

  @Override
  public void info(Throwable error) {
    logs.add(new LogEvent(INFO, null, error));
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public void warn(CharSequence content) {
    logs.add(new LogEvent(WARN, content.toString(), null));
  }

  @Override
  public void warn(CharSequence content, Throwable error) {
    logs.add(new LogEvent(WARN, content.toString(), error));
  }

  @Override
  public void warn(Throwable error) {
    logs.add(new LogEvent(WARN, null, error));
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public void error(CharSequence content) {
    logs.add(new LogEvent(ERROR, content.toString(), null));
  }

  @Override
  public void error(CharSequence content, Throwable error) {
    logs.add(new LogEvent(ERROR, content.toString(), error));
  }

  @Override
  public void error(Throwable error) {
     logs.add(new LogEvent(ERROR, null, error));
  }

}
