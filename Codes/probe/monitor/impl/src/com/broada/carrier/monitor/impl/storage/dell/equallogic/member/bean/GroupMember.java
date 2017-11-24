package com.broada.carrier.monitor.impl.storage.dell.equallogic.member.bean;

public class GroupMember {
	//是否监测
	private Boolean isWacthed = Boolean.TRUE;
	//成员名称
	private String memberName;
	//理想状态
	private String memberState;
	//当前状态
	private String memberInfoState;
	// 成员设备磁盘阵列的型号
	private String memberModel;
	// 成员设备磁盘阵列的序列号
	private String memberSeriesNumber;
	// 成员设备磁盘数量
	private Integer memberDiskNumber;
	// 成员设备磁盘总大小
	private Double memberTotalStorage;
	//阵列已使用空间大小
	private Double memberUsedStorage;
	//读操作数
//	private long memberReadOpCount;
//	//写操作数
//	private long memberWriteOpCount;
	//RAID版本
	private String driveGroupRAIDPolicy;
	//预阀值
//	private String checkMemberInfoState;
	//GET/SET方法
	
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public String getMemberModel() {
		return memberModel;
	}
	public void setMemberModel(String memberModel) {
		this.memberModel = memberModel;
	}
	public String getMemberSeriesNumber() {
		return memberSeriesNumber;
	}
	public void setMemberSeriesNumber(String memberSeriesNumber) {
		this.memberSeriesNumber = memberSeriesNumber;
	}
//	public Integer getMemberControllerNumber() {
//		return memberControllerNumber;
//	}
//	public void setMemberControllerNumber(Integer memberControllerNumber) {
//		this.memberControllerNumber = memberControllerNumber;
//	}
	public Integer getMemberDiskNumber() {
		return memberDiskNumber;
	}
	public void setMemberDiskNumber(Integer memberDiskNumber) {
		this.memberDiskNumber = memberDiskNumber;
	}
	public Double getMemberTotalStorage() {
		return memberTotalStorage;
	}
	public void setMemberTotalStorage(Double memberTotalStorage) {
		this.memberTotalStorage = memberTotalStorage;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	public String getMemberState() {
		return memberState;
	}
	public void setMemberState(String memberState) {
		this.memberState = memberState;
	}
	public String getMemberInfoState() {
		return memberInfoState;
	}
	public void setMemberInfoState(String memberInfoState) {
		this.memberInfoState = memberInfoState;
	}
	public Double getMemberUsedStorage() {
		return memberUsedStorage;
	}
	public void setMemberUsedStorage(Double memberUsedStorage) {
		this.memberUsedStorage = memberUsedStorage;
	}
	public String getDriveGroupRAIDPolicy() {
		return driveGroupRAIDPolicy;
	}
	public void setDriveGroupRAIDPolicy(String driveGroupRAIDPolicy) {
		this.driveGroupRAIDPolicy = driveGroupRAIDPolicy;
	}
//	public String getCheckMemberInfoState() {
//		return checkMemberInfoState;
//	}
//	public void setCheckMemberInfoState(String checkMemberInfoState) {
//		this.checkMemberInfoState = checkMemberInfoState;
//	}
	
}
