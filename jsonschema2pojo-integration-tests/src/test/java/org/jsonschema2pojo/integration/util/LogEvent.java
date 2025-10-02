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

import java.util.Optional;

public class LogEvent {
  public static enum Level {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  public static LogEvent debug(String message) {
    return new LogEvent(Level.DEBUG, message, null);
  }

  protected Level level;
  protected String message;
  protected Throwable error;

  public LogEvent(Level level, String message, Throwable error) {
    this.level = level;
    this.message = message;
    this.error = error;
  }

  public Level getLevel() {
    return level;
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  public Optional<Throwable> getError() {
    return Optional.ofNullable(error);
  }


}
