package com.broada.carrier.monitor.impl.common;

/**
 * 进行采集时发生的异常
 * @author Maico(panghf@broada.com)
 * Create By 2011-8-16 上午02:38:23
 */
public class CollectException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
   * 
   */
  public CollectException() {
  }

  /**
   * @param message
   */
  public CollectException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public CollectException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public CollectException(String message, Throwable cause) {
    super(message, cause);
  }

}
