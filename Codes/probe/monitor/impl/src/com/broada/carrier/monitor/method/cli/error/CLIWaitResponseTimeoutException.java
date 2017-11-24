package com.broada.carrier.monitor.method.cli.error;


public class CLIWaitResponseTimeoutException extends CLIException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -5584611347684808648L;

  public CLIWaitResponseTimeoutException() {
    super();
  }

  public CLIWaitResponseTimeoutException(String message) {
    super(message);
  }

  public CLIWaitResponseTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLIWaitResponseTimeoutException(Throwable cause) {
    super(cause);
  }
}
