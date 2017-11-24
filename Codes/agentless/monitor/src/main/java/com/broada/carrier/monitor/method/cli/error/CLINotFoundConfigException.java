package com.broada.carrier.monitor.method.cli.error;

public class CLINotFoundConfigException extends CLIException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 1401928868966705322L;

  public CLINotFoundConfigException() {
    super();
  }

  public CLINotFoundConfigException(String message) {
    super(message);
  }

  public CLINotFoundConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLINotFoundConfigException(Throwable cause) {
    super(cause);
  }
}
