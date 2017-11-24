package com.broada.carrier.monitor.impl.host.cli.info;

public class CLIHostInfoMonitorCondition {
	private String field;
	private String value;

	public CLIHostInfoMonitorCondition() {
	}

	public CLIHostInfoMonitorCondition(String field, String value) {
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return field + " : " + value;
	}

	public String getFieldCondition() {
		return "";
	}

	public String getFieldDescription() {
		return field;
	}

	public String getFieldName() {
		return field;
	}

}
