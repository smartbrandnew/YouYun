package com.broada.carrier.monitor.impl.mw.webspheremq;

import com.ibm.mq.MQException;

/**
 * <p>Title: IbmMqException</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class IbmMqException extends Exception{
  private static final long serialVersionUID = 2641339935472068389L;
  private MQException mqe;
  
  public MQException getMQException() {
    return mqe;
  }

  public void setMQException(MQException mqe) {
    this.mqe = mqe;
  }

  private void innerSetMQException(Throwable e) {
    if(e instanceof MQException){
      this.mqe=(MQException)e;
    }
  }
  
  public IbmMqException() {
    super();
  }

  public IbmMqException(String s) {
    super(s);
  }

  public IbmMqException(String s, Throwable e) {
    super(s, e);
    innerSetMQException(e);
  }

  public IbmMqException(Throwable e) {
    super(e);
    innerSetMQException(e);
  }

  @Override
  public synchronized Throwable initCause(Throwable cause) {
    innerSetMQException(cause);
    return super.initCause(cause);
  }
}

