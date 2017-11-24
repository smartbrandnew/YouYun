package com.broada.carrier.monitor.impl.virtual.hypervisor.info;

import com.broada.utils.Condition;

public class CLIHyperVInfoMonitorCondition extends Condition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean select = Boolean.FALSE;

	private boolean validate = true;

	// 名称
	private String name;
	
	private String field;
	// 安装时间
	private String installDate;
	// 健康状态
	private String healthState;
	// 当前状态
	private String currentState;
	// 运行时间
	private String onLineTime;
	// 虚拟机类型
	private String caption;
	//最后一次状态改变时间
	private String timeOfLastStateChange;
	//操作系统版本
	private String guestOperatingSystem;
	//备注
	private String notes;
	//分配的内存空间
	private String memoryUsage;
	//CPU核心数
	private String numberOfProcessors;
	//
	private String upTime;
	//
	private String processorLoad;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public CLIHyperVInfoMonitorCondition() {
		type = Condition.LESSTHAN;
	}

	public String getName() {
		return name;
	}

	public Boolean getSelect() {
		return select;
	}

	public void setSelect(Boolean select) {
		this.select = select;
	}

	public void setSelect(String select) {
		this.select = select.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public String getInstallDate() {
		return installDate;
	}

	public void setInstallDate(String installDate) {
		this.installDate = installDate;
	}

	public String getHealthState() {
		return healthState;
	}

	public void setHealthState(String healthState) {
		this.healthState = healthState;
	}

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public String getOnLineTime() {
		return onLineTime;
	}

	public void setOnLineTime(String onLineTime) {
		this.onLineTime = onLineTime;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getTimeOfLastStateChange() {
		return timeOfLastStateChange;
	}

	public void setTimeOfLastStateChange(String timeOfLastStateChange) {
		this.timeOfLastStateChange = timeOfLastStateChange;
	}

	public String getGuestOperatingSystem() {
		return guestOperatingSystem;
	}

	public void setGuestOperatingSystem(String guestOperatingSystem) {
		this.guestOperatingSystem = guestOperatingSystem;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(String memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

	public String getNumberOfProcessors() {
		return numberOfProcessors;
	}

	public void setNumberOfProcessors(String numberOfProcessors) {
		this.numberOfProcessors = numberOfProcessors;
	}

	public String getUpTime() {
		return upTime;
	}

	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}

	public String getProcessorLoad() {
		return processorLoad;
	}

	public void setProcessorLoad(String processorLoad) {
		this.processorLoad = processorLoad;
	}

}
