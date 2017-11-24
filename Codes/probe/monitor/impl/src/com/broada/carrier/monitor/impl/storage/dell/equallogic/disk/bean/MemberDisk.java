package com.broada.carrier.monitor.impl.storage.dell.equallogic.disk.bean;

public class MemberDisk {
	// 是否监测
	private Boolean isWacthed = Boolean.TRUE;
	// 磁盘编号
	private String diskId;
	// 磁盘SlotID
//	private String slotID;
	// //磁盘类型
	// private String diskType;
	// 修订版本
//	private String revNum;
	// 磁盘序列号
//	private String serialNumber;
	// 磁盘状态
	private String diskStatus;
	// 磁盘大小
//	private Double diskSize;
	// 磁盘错误数
//	private Integer diskErrors;
	// 所属成员设备名称
	private String memberName;
	//磁盘状态预阀值
	private String CheckDiskStatus = "on-line";

	// GET and SET
	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getDiskId() {
		return diskId;
	}

	public void setDiskId(String diskId) {
		this.diskId = diskId;
	}

	public String getDiskStatus() {
		return diskStatus;
	}

	public void setDiskStatus(String diskStatus) {
		this.diskStatus = diskStatus;
	}


	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getCheckDiskStatus() {
		return CheckDiskStatus;
	}

	public void setCheckDiskStatus(String checkDiskStatus) {
		CheckDiskStatus = checkDiskStatus;
	}
	
	

}
