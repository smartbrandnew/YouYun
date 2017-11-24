package com.broada.carrier.monitor.client.impl.impexp.error;

public class NeedOptionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String option;

	public NeedOptionException(String option, Throwable e) {
		super(e);
		this.option = option;
	}

	public String getOption() {
		return option;
	}

}
