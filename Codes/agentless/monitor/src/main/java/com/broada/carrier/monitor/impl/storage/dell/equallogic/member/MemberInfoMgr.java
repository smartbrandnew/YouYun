package com.broada.carrier.monitor.impl.storage.dell.equallogic.member;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.member.bean.GroupMember;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;

public class MemberInfoMgr {
	private static final Log logger = LogFactory.getLog(MemberInfoMgr.class);
	
	protected static final String OID_MEM_NAME=".1.3.6.1.4.1.12740.1.1.1.1.19.1";
	
	protected static final String OID_MEM_STATE=".1.3.6.1.4.1.12740.2.1.1.1.11.1";
	
	protected static final String OID_MEM_INFOSTATE=".1.3.6.1.4.1.12740.2.1.4.1.2.1";
	
	protected static final String OID_MEM_USEDSTORAGE=".1.3.6.1.4.1.12740.2.1.10.1.2.1";
	
	protected static final String OID_MEM_MODLE=".1.3.6.1.4.1.12740.2.1.11.1.1.1";
	
	protected static final String OID_MEM_SERNUM=".1.3.6.1.4.1.12740.2.1.11.1.2.1";
	
	protected static final String OID_MEM_DISKNUM=".1.3.6.1.4.1.12740.2.1.11.1.4.1";
	
	protected static final String OID_MEM_TOTALSTORAGE=".1.3.6.1.4.1.12740.2.1.10.1.1.1";
	
	protected static final String OID_MEM_DRIVEGROUPRAIDPOLICY=".1.3.6.1.4.1.12740.2.1.15.1.3.1";
	
	protected int version;

	protected String ipAddr;

	protected int port;

	protected String community;

	protected int timeout;

	protected SnmpWalk walk = null;

	public MemberInfoMgr(SnmpWalk walk) {
		this.walk = walk;
	}

	public void close(){
		if(walk != null)
			walk.close();
	}
	
	public List<GroupMember> generateMemberInfo() throws Exception {
		List<GroupMember> memberInfos = new ArrayList<GroupMember>();
		// 获取Group 成员 的扩展 OID
		String[] extendOIDs = getMemExtendOIDs();
		if(extendOIDs.length == 1){
			GroupMember member = new GroupMember();
			member.setMemberName(getMemberName(getMemExtendOID(OID_MEM_NAME)));
			logger.info("getMemberName:"+getMemberName(getMemExtendOID(OID_MEM_NAME)));
			logger.info("getTotalStorage:"+getTotalStorage(getMemExtendOID(OID_MEM_TOTALSTORAGE)));
			member.setMemberState(getMemberStatus(getMemExtendOID(OID_MEM_STATE)));
			member.setMemberInfoState(getMemberInfoStatus(getMemExtendOID(OID_MEM_INFOSTATE)));
			member.setMemberModel(getMemberModel(getMemExtendOID(OID_MEM_MODLE)));
			member.setMemberSeriesNumber(getSerialNumber(getMemExtendOID(OID_MEM_SERNUM)));
			member.setMemberDiskNumber(getDiskNumber(getMemExtendOID(OID_MEM_DISKNUM)));
			member.setMemberTotalStorage(getTotalStorage(getMemExtendOID(OID_MEM_TOTALSTORAGE)));
			member.setMemberUsedStorage(getUserStorage(getMemExtendOID(OID_MEM_USEDSTORAGE)));
			member.setDriveGroupRAIDPolicy(getDriveGroupRAIDPolicy(getMemExtendOID(OID_MEM_DRIVEGROUPRAIDPOLICY)));
			memberInfos.add(member);
		}else{
		for (int i = 0; i < extendOIDs.length; i++) {
			GroupMember member = new GroupMember();
			member.setMemberName(getMemberName(extendOIDs[i]));
			logger.info("getMemberName:"+getMemberName(extendOIDs[i]));
			logger.info("getTotalStorage:"+getTotalStorage(extendOIDs[i]));
			member.setMemberState(getMemberStatus(extendOIDs[i]));
			member.setMemberInfoState(getMemberInfoStatus(extendOIDs[i]));
			member.setMemberModel(getMemberModel(extendOIDs[i]));
			member.setMemberSeriesNumber(getSerialNumber(extendOIDs[i]));
			member.setMemberDiskNumber(getDiskNumber(extendOIDs[i]));
			member.setMemberTotalStorage(getTotalStorage(extendOIDs[i]));
			member.setMemberUsedStorage(getUserStorage(extendOIDs[i]));
			member.setDriveGroupRAIDPolicy(getDriveGroupRAIDPolicy(extendOIDs[i]));
			memberInfos.add(member);
		}
		}
		return memberInfos;
	}
	
	private String getMemberName(String extendOID) throws Exception {
		String memberName = "";
		try{
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_NAME + extendOID);
			if(snmpValue != null){
				memberName = snmpValue.toString();
			}
		}catch(SnmpException e){
			logger.error("获取成员名称失败",e);
			throw new Exception("成员名称获取失败，" + e.getMessage() + "。", e);
		}
		return memberName;
	}
	
	private String getMemberStatus (String extendOID) throws Exception {
		String memberStatus = "";
		try{
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_STATE + extendOID);
			if(snmpValue != null){
				memberStatus = snmpValue.toString();
			}
		}catch(SnmpException e){
			logger.error("获取成员理想状态失败",e);
			throw new Exception("成员理想状态获取失败，" + e.getMessage() + "。", e);
		}
		return memberStatus;
	}
	
	private String getMemberInfoStatus (String extendOID) throws Exception {
		String memberInfoStatus = "";
		try{
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_INFOSTATE + extendOID);
			if(snmpValue != null){
				int status = (int) snmpValue.toLong();
				switch (status){
				case 1:
					memberInfoStatus = "on-line";
					break;
				case 2:
					memberInfoStatus = "off-line";
					break;
				case 3:
					memberInfoStatus = "vacating-in-progress";
					break;
				case 4:
					memberInfoStatus = "vacate";
					break;
				default:
					break;
				}
			}
		}catch(SnmpException e){
			logger.error("获取成员当前状态失败。", e);
			throw new Exception("成员当前状态获取失败，" + e.getMessage() + "。", e);
		}
		return memberInfoStatus;
	}
	
	private String getMemberModel(String extendOID) throws Exception {
		String memberModel = "";
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_MODLE + extendOID);
			if (snmpValue != null) {
				memberModel = snmpValue.toString();
			}
		} catch (SnmpException e) {
			logger.error("获取成员设备型号失败。", e);
			throw new Exception("成员设备型号获取失败，" + e.getMessage() + "。", e);
		}
		return memberModel;
	}
	
	private String getSerialNumber(String extendOID) throws Exception {
		String serialNumber = "";
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_SERNUM + extendOID);
			if (snmpValue != null) {
				serialNumber = snmpValue.toString();
			}
		} catch (SnmpException e) {
			logger.error("获取成员设备序列号失败。", e);
			throw new Exception("成员设备序列号获取失败，" + e.getMessage() + "。", e);
		}
		return serialNumber;
	}
	
	private Double getTotalStorage(String extendOID) throws Exception {
		double totalStorage = 0;
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_TOTALSTORAGE
					+ extendOID);
			if (snmpValue != null) {
				totalStorage = new BigDecimal(snmpValue.toLong()
						/ (1024.0 * 1024.0)).setScale(2,
						BigDecimal.ROUND_HALF_UP).doubleValue();
			}
		} catch (SnmpException e) {
			logger.error("获取磁盘总大小失败。", e);
			throw new Exception("磁盘总大小获取失败，" + e.getMessage() + "。", e);
		}
		return totalStorage;
	}
	
	private Integer getDiskNumber(String extendOID) throws Exception {
		double diskNumber = 0;
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_DISKNUM + extendOID);
			if (snmpValue != null) {
				diskNumber = snmpValue.toLong();
			}
		} catch (SnmpException e) {
			logger.error("获取磁盘总数失败。", e);
			throw new Exception("磁盘总数获取失败，" + e.getMessage() + "。", e);
		}
		return (int) diskNumber;
	}
	
	private Double getUserStorage(String extendOID) throws Exception {
		double usedStorage = 0;
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_USEDSTORAGE + extendOID);
			if(snmpValue != null){
				usedStorage = new BigDecimal(snmpValue.toLong()/(1024.0*1024.0)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
			}
		}catch(SnmpException e){
			logger.error("获取磁盘已使用大小失败。", e);
			throw new Exception("磁盘已使用大小获取失败，" + e.getMessage() + "。", e);
		}
		return usedStorage;
	}
	
//	private long getReadOpCount(String extendOID) throws Exception {
//		long readOpCount = 0;
//		try{
//			SnmpValue snmpValue = getStrValueByOid(OID_MEM_READOPCOUNT + extendOID);
//			if(snmpValue != null){
//				readOpCount = snmpValue.toLong();
//			}
//		}catch(SnmpException e){
//			logger.error("获取读操作数失败。",e);
//			throw new Exception ("读操作数获取失败，"+e.getMessage()+"。",e);
//		}
//		return readOpCount;
//	}
//	
//	private long getWriteOpCount(String extendOID)throws Exception{
//		long writeOpCount = 0;
//		return writeOpCount;
//	}
	
	private String getDriveGroupRAIDPolicy(String extendOID) throws Exception{
		String DriveGroupRAIDPolicy = "";
		try{
			SnmpValue snmpValue = getStrValueByOid(OID_MEM_DRIVEGROUPRAIDPOLICY + extendOID);
			if(snmpValue != null){
				DriveGroupRAIDPolicy = snmpValue.toString();
			}
		}catch(SnmpException e){
			logger.error("获取RAID版本失败。", e);
			throw new Exception("RAID版本获取失败，" + e.getMessage() + "。", e);
		}
		return DriveGroupRAIDPolicy;
	}
	
	protected SnmpValue getStrValueByOid (String oid) throws SnmpException {
		SnmpResult var = null;
		try{
			var = walk.snmpGet(oid);
		}catch(SnmpException e){
			throw e;
		}
		if(var != null){
			return var.getValue();
		}else {
			return null;
		}
	}
	
	public String[] getMemExtendOIDs() throws SnmpException{
		SnmpResult[] snmpresults = null;
		try{
			snmpresults = walk.snmpWalk(OID_MEM_NAME);
		}catch (SnmpException e){
			throw e;
		}
		String[] OIDs = new String[snmpresults.length];
		
			for (int i = 0; i < snmpresults.length; i++){
				SnmpResult snmpresult = snmpresults[i];
				String oid = snmpresult.getOid().toString();
				OIDs[i] = oid.substring(oid.lastIndexOf("."), oid.length());
			}
		return OIDs;
	}
	
	public String getMemExtendOID(String oid) throws SnmpException{
		SnmpResult[] snmpresult = null;
		try{
			snmpresult = walk.snmpWalk(oid);
			String varOid = snmpresult[0].getOid().toString();
			return varOid.substring(varOid.lastIndexOf(".", oid.length()));
		}catch(SnmpException e){
			throw e;
		}
		
		
	}
}
