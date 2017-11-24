package com.broada.carrier.monitor.impl.storage.dell.equallogic.fan.bean;

public class MemberFan {
	// 扩展OID
	private String extendOid;
	// 是否监测
	private Boolean isWacthed = Boolean.TRUE;
	// 风扇名称
	private String fanName;
	// 风扇状态
	private String fanStatus;
	// 所属成员设备名称
	private String memberName;
	//风扇状态（预阀值）
//	private String CheckFanStatus;

	@Override
	public String toString() {
		return this.fanName + "==" + this.fanStatus
				+ "==" + this.memberName;
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

	public String getFanName() {
		return fanName;
	}

	public void setFanName(String fanName) {
		this.fanName = fanName;
	}

	public String getFanStatus() {
		return fanStatus;
	}

	public void setFanStatus(String fanStatus) {
		this.fanStatus = fanStatus;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

//	public String getCheckFanStatus() {
//		return CheckFanStatus;
//	}
//
//	public void setCheckFanStatus(String checkFanStatus) {
//		CheckFanStatus = checkFanStatus;
//	}

	
}
