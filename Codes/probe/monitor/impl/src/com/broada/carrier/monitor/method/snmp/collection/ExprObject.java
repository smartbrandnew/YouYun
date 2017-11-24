package com.broada.carrier.monitor.method.snmp.collection;

import java.io.Serializable;

public class ExprObject implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorInst = null;// 监测实例
	private String monitorName = null;// 监测名称
	private double value;
	private String expression = null;// 表达式

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getMonitorInst() {
		return monitorInst;
	}

	public void setMonitorInst(String monitorInst) {
		this.monitorInst = monitorInst;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
