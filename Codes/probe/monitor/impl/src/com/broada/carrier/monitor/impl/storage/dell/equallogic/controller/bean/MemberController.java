package com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.bean;

public class MemberController {
	// 是否监测
	private Boolean isWacthed = Boolean.TRUE;
	// 控制器序列号
	private String serialNumber;
	// 控制器版本号
	private String contrRevision;
	// 主控制器或副控制器
	private String contrPrimOrSec;
	// 控制器类型
	private String contrType;
	// 控制器启动时间
//	private String contrBootTime;
	// 处理器温度（℃）
	private Double ProcessorTemp;
	// 芯片温度（℃）
	private Double ChipsetTemp;
	// 电池状态
	private String batteryStatus;
	// 物理内存大小(MB)
//	private Double contrPhysRam;
	// 所属成员设备名称
	private String memberName;
	//预阀值(芯片温度(℃))
	Double checkProcsetTemp = new Double(60);

	// GET SET
	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getContrRevision() {
		return contrRevision;
	}

	public void setContrRevision(String contrRevision) {
		this.contrRevision = contrRevision;
	}

	public String getContrType() {
		return contrType;
	}

	public void setContrType(String contrType) {
		this.contrType = contrType;
	}

	public String getContrPrimOrSec() {
		return contrPrimOrSec;
	}

	public void setContrPrimOrSec(String contrPrimOrSec) {
		this.contrPrimOrSec = contrPrimOrSec;
	}

//	public String getContrBootTime() {
//		return contrBootTime;
//	}

//	public void setContrBootTime(String contrBootTime) {
//		this.contrBootTime = contrBootTime;
//	}

	public Double getProcessorTemp() {
		return ProcessorTemp;
	}

	public void setProcessorTemp(Double processorTemp) {
		ProcessorTemp = processorTemp;
	}

	public Double getChipsetTemp() {
		return ChipsetTemp;
	}

	public void setChipsetTemp(Double chipsetTemp) {
		ChipsetTemp = chipsetTemp;
	}

	public String getBatteryStatus() {
		return batteryStatus;
	}

	public void setBatteryStatus(String batteryStatus) {
		this.batteryStatus = batteryStatus;
	}
	
	

	public Double getCheckProcsetTemp() {
		return checkProcsetTemp;
	}

	public void setCheckProcsetTemp(Double checkProcsetTemp) {
		this.checkProcsetTemp = checkProcsetTemp;
	}

	//	public Double getContrPhysRam() {
//		return contrPhysRam;
//	}
//
//	public void setContrPhysRam(Double contrPhysRam) {
//		this.contrPhysRam = contrPhysRam;
//	}
//
	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

}
