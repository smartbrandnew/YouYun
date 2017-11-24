package com.broada.carrier.monitor.method.cli.error;

/**
 * 作为判断连接是否成功、失效的依据
 * 
 */
public class CLIConnectException extends CLIException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 3699613922663890456L;

  public CLIConnectException() {
    super();
  }

  public CLIConnectException(String message) {
    super(message);
  }

  public CLIConnectException(String message, Throwable cause) {
    super(message, cause);
  }

  public CLIConnectException(Throwable cause) {
    super(cause);
  }
}
