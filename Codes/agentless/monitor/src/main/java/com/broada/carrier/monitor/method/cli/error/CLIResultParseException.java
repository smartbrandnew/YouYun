package com.broada.carrier.monitor.method.cli.error;

/**
 * CLI采集结果解析异常
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-6-13 19:18:56
 */
public class CLIResultParseException extends CLIException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 4752356780636299686L;

  /**
   * 
   */
  public CLIResultParseException() {
    super();
  }

  /**
   * @param message
   */
  public CLIResultParseException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public CLIResultParseException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public CLIResultParseException(Throwable cause) {
    super(cause);
  }

}
