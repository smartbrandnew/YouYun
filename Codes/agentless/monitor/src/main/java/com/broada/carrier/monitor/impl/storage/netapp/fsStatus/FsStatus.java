package com.broada.carrier.monitor.impl.storage.netapp.fsStatus;

public class FsStatus {
	private Boolean isWacthed = Boolean.FALSE;

	private String monitorInst = null;// 监测实例

	private String monitorName = null;// 状态名称

	// private Double currentValue = null;//当前值
	
	private String monitorItem = null;// 状态名称

	private Integer vavel = null; // 状态值

	private String expression = null;// 表达式

	public String getMonitorItem() {
		return monitorItem;
	}

	public void setMonitorItem(String monitorItem) {
		this.monitorItem = monitorItem;
	}

	public Integer getVavel() {
		return vavel;
	}

	public void setVavel(Integer vavel) {
		this.vavel = vavel;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getMonitorName() {
		return monitorName;
	}

	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	public String getMonitorInst() {
		return monitorInst;
	}

	public void setMonitorInst(String monitorInst) {
		this.monitorInst = monitorInst;
	}
}
