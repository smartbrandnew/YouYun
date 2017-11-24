package com.broada.carrier.monitor.impl.mw.jboss;

import java.io.PrintStream;
import java.io.PrintWriter;

public class JbossRemoteException extends Exception {

	private static final long serialVersionUID = 1L;

	private String detail = "";

	public JbossRemoteException(String message) {
		super(message);
	}

	public JbossRemoteException(String message, String detail) {
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
