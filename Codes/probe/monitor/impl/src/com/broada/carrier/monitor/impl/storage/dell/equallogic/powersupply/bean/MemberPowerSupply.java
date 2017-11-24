package com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply.bean;

public class MemberPowerSupply {
	// 扩展OID
	private String extendOid;
	// 是否监测
	private Boolean isWacthed = Boolean.TRUE;
	// 电源名称
	private String powerSupplyName;
	// 电源状态
	private String powerSupplyStatus;
	// 电源风扇状态
//	private String PowerSupplyFanStatus;
	// 所属成员设备名称
	private String memberName;
	//预阀值(电源状态)
	private String checkPowerSupplyStatus;

	@Override
	public String toString() {
		return this.powerSupplyName + "==" + this.powerSupplyStatus +  "==" + this.memberName;
	}

	// GET SET

	public String getExtendOid() {
		return extendOid;
	}

	public void setExtendOid(String extendOid) {
		this.extendOid = extendOid;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getPowerSupplyName() {
		return powerSupplyName;
	}

	public void setPowerSupplyName(String powerSupplyName) {
		this.powerSupplyName = powerSupplyName;
	}

	public String getPowerSupplyStatus() {
		return powerSupplyStatus;
	}

	public void setPowerSupplyStatus(String powerSupplyStatus) {
		this.powerSupplyStatus = powerSupplyStatus;
	}

	
	
	public String getCheckPowerSupplyStatus() {
		return checkPowerSupplyStatus;
	}

	public void setCheckPowerSupplyStatus(String checkPowerSupplyStatus) {
		this.checkPowerSupplyStatus = checkPowerSupplyStatus;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
}
