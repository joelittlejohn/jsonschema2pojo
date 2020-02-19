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

package org.jsonschema2pojo.cli;

import com.beust.jcommander.IParameterValidator2;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jsonschema2pojo.AbstractRuleLogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandLineLogger extends AbstractRuleLogger {

  public static final String DEFAULT_LOG_LEVEL = LogLevel.INFO.value();

  private final int logLevel;

  public CommandLineLogger(String logLevel) {
    this.logLevel = LogLevel.fromValue(logLevel).levelInt();
  }

  @Override
  public boolean isDebugEnabled() {
    return logLevel >= LogLevel.DEBUG.levelInt();
  }

  @Override
  public boolean isErrorEnabled() {
    return logLevel >= LogLevel.ERROR.levelInt();
  }

  @Override
  public boolean isInfoEnabled() {
    return logLevel >= LogLevel.INFO.levelInt();
  }

  @Override
  public boolean isTraceEnabled() {
    return logLevel >= LogLevel.TRACE.levelInt();
  }

  @Override
  public boolean isWarnEnabled() {
    return logLevel >= LogLevel.WARN.levelInt();
  }

  public void printLogLevels() {
    Set<String> levelNames = LogLevel.getLevelNames();
    String levelNamesJoined = levelNames.stream().collect(Collectors.joining(", "));
    System.out.println("Available Log Levels: " + levelNamesJoined);
  }

  @Override
  protected void doDebug(String msg) {
    System.out.println(msg);
  }

  @Override
  protected void doError(String msg, Throwable e) {
    System.err.println(msg);
    if(e != null) {
      e.printStackTrace(System.err);
    }
  }

  @Override
  protected void doInfo(String msg) {
    System.out.print(msg);
  }

  @Override
  protected void doTrace(String msg) {
    System.out.print(msg);
  }

  @Override
  protected void doWarn(String msg, Throwable e) {
    System.err.println(msg);
    if(e != null) {
      e.printStackTrace(System.err);
    }
  }

  public enum LogLevel {
    OFF("off", -2),
    ERROR("error", -1),
    WARN("warn", 0),
    INFO("info", 1),
    DEBUG("debug", 2),
    TRACE("trace", 3);

    private final static Map<String, LogLevel> LEVEL_NAMES = new LinkedHashMap<>();
    private final String levelName;
    private final int levelInt;

    LogLevel(String value, int levelInt) {
      this.levelName = value;
      this.levelInt = levelInt;
    }

    @JsonCreator
    public static LogLevel fromValue(String value) {
      LogLevel constant = LEVEL_NAMES.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

    public static Set<String> getLevelNames() {
      return LEVEL_NAMES.keySet();
    }

    public int levelInt() {
      return levelInt;
    }

    @Override
    public String toString() {
      return this.levelName;
    }

    @JsonValue
    public String value() {
      return this.levelName;
    }

    static {
      for (LogLevel c : values()) {
        LEVEL_NAMES.put(c.levelName, c);
      }
    }
  }

  public static class LogLevelValidator implements IParameterValidator2 {

    @Override
    public void validate(String name, String value, ParameterDescription pd) throws ParameterException {

      Collection<String> availableLogLevels = LogLevel.getLevelNames();

      if (!availableLogLevels.contains(value)) {
        String availableLevelJoined = availableLogLevels.stream().collect(Collectors.joining(", ", "[", "]"));
        throw new ParameterException("The parameter " + name + " must be one of " + availableLevelJoined);
      }
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
      validate(name, value, null);
    }
  }
}
