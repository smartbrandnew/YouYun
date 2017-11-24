package com.broada.carrier.monitor.impl.generic;

import java.io.Serializable;

public class GenericCollectResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private String executeText;

	public String getExecuteText() {
		return executeText;
	}

	public void setExecuteText(String executeText) {
		this.executeText = executeText;
	}

}
