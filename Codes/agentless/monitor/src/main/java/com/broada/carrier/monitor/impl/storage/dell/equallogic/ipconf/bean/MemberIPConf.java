package com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.bean;

public class MemberIPConf {
	// 扩展OID
	private String extendOid;
	// 是否监测
	private Boolean isWacthed = Boolean.TRUE;
	// 网络接口编号
	private String interfaceIndex;
	// 网络接口名称
	private String interfaceName;
	// IP地址
	private String ipAddress;
	// 子网掩码
	private String subNetMask;
	// 状态
	private String status;
	// 所属成员设备名称
	private String memberName;
	//接口状态
//	private String CheckStatus;
	@Override
	public String toString() {
		return this.interfaceName + "==" + this.ipAddress + "=="
				+ this.subNetMask + "==" + this.memberName;
	}

	// GET SET

	public String getExtendOid() {
		return extendOid;
	}

	public String getInterfaceIndex() {
		return interfaceIndex;
	}

	public void setInterfaceIndex(String interfaceIndex) {
		this.interfaceIndex = interfaceIndex;
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

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSubNetMask() {
		return subNetMask;
	}

	public void setSubNetMask(String subNetMask) {
		this.subNetMask = subNetMask;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

//	public String getCheckStatus() {
//		return CheckStatus;
//	}
//
//	public void setCheckStatus(String checkStatus) {
//		CheckStatus = checkStatus;
//	}
	
}
