package com.broada.carrier.monitor.method.cli.error;

public class CLIException extends RuntimeException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2643150435510172635L;

  public CLIException() {
    super();
  }

  public CLIException(String message) {
    super(message);
  }

  public CLIException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLIException(Throwable cause) {
    super(cause);
  }
}
