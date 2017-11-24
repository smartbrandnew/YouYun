package com.broada.carrier.monitor.method.cli.error;

public class CLIRuntimeException extends RuntimeException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 4650932972495491597L;

  public CLIRuntimeException() {
    super();
  }

  public CLIRuntimeException(String message) {
    super(message);
  }

  public CLIRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLIRuntimeException(Throwable cause) {
    super(cause);
  }
}
