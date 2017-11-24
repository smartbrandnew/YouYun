package com.broada.carrier.monitor.impl.mw.tongweb;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MBeanException extends Exception{
	private static final long serialVersionUID = 1L;
	private String detail = "";
  public MBeanException(String message){
    super(message);
  }
  
  public MBeanException() {
    super();
  }

  public MBeanException(String message, Throwable cause) {
    super(message, cause);
  }

  public MBeanException(Throwable cause) {
    super(cause);
  }

  public MBeanException(String message, String detail){
    super(message);
    this.detail = detail;
  }

  public void printStackTrace(PrintWriter s) {
    super.printStackTrace(s);
    s.println(detail);
    s.flush();
  }

  public void printStackTrace(PrintStream s) {
    super.printStackTrace(s);
    s.print(detail);
    s.flush();
  }

  public String toString() {
    return super.toString() + "\n" + detail;
  }
}
