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
      doError(msg);
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
      doWarn(msg);
    }
  }

  abstract protected void doDebug(String msg);

  abstract protected void doError(String msg);

  abstract protected void doInfo(String msg);

  abstract protected void doTrace(String msg);

  abstract protected void doWarn(String msg);
}
