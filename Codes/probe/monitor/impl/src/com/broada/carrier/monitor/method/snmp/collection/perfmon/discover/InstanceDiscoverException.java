package com.broada.carrier.monitor.method.snmp.collection.perfmon.discover;

/**
 * 
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-24 15:02:52
 */
public class InstanceDiscoverException extends Exception {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -8673835882871892664L;

  /**
   * 
   */
  public InstanceDiscoverException() {
    super();
  }

  /**
   * @param message
   */
  public InstanceDiscoverException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public InstanceDiscoverException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public InstanceDiscoverException(String message, Throwable cause) {
    super(message, cause);
  }

}
