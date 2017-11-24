package com.broada.carrier.monitor.method.cli.error;


public class CLILoginFailException extends CLIException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -5584611347684808648L;

  public CLILoginFailException() {
    super();
  }

  public CLILoginFailException(String message) {
    super(message);
  }

  public CLILoginFailException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLILoginFailException(Throwable cause) {
    super(cause);
  }
}
