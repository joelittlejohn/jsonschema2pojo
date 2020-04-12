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

package org.jsonschema2pojo;

public abstract class AbstractRuleLogger implements RuleLogger {
  @Override
  public void debug(String msg) {
    if (isDebugEnabled()) {
      doDebug(msg);
    }
  }

  @Override
  public void error(String msg) {
    if (isErrorEnabled()) {
      doError(msg, null);
    }
  }

  @Override
  public void error(String msg, Throwable e) {
    if (isErrorEnabled()) {
      doError(msg, e);
    }
  }

  @Override
  public void info(String msg) {
    if (isInfoEnabled()) {
      doInfo(msg);
    }
  }

  @Override
  public void trace(String msg) {
    if (isTraceEnabled()) {
      doTrace(msg);
    }
  }

  @Override
  public void warn(String msg) {
    if (isWarnEnabled()) {
      doWarn(msg, null);
    }
  }

  @Override
  public void warn(String msg, Throwable e) {
    if (isWarnEnabled()) {
      doWarn(msg, e);
    }
  }

  abstract protected void doDebug(String msg);

  abstract protected void doError(String msg, Throwable e);

  abstract protected void doInfo(String msg);

  abstract protected void doTrace(String msg);

  abstract protected void doWarn(String msg, Throwable e);
}
