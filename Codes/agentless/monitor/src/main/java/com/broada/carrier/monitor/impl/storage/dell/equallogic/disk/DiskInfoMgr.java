package com.broada.carrier.monitor.impl.storage.dell.equallogic.disk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.disk.bean.MemberDisk;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpRow;
import com.broada.snmputil.SnmpTable;
import com.broada.snmputil.SnmpTarget;

/**
 * 
 * @author shoulw
 */
public class DiskInfoMgr {
	private static final Log logger = LogFactory.getLog(DiskInfoMgr.class);
	// 磁盘编号
	protected static final SnmpOID OID_DISK_ID = new SnmpOID(
			".1.3.6.1.4.1.12740.3.1.1.1.10.1");
	// 磁盘slotID
//	protected static final SnmpOID OID_DISK_SLOT = new SnmpOID(
//			".1.3.6.1.4.1.12740.3.1.1.1.11.1");
	// // 磁盘类型
	// protected static final SnmpOID OID_DISK_TYPE = new SnmpOID(
	// ".1.3.6.1.4.1.12740.3.1.1.1.2.1");
	// 修订版本
//	protected static final SnmpOID OID_DISK_REVNUM = new SnmpOID(
//			".1.3.6.1.4.1.12740.3.1.1.1.4.1");
	// 磁盘序列号
//	protected static final SnmpOID OID_DISK_SERNUM = new SnmpOID(
//			".1.3.6.1.4.1.12740.3.1.1.1.5.1");
	// 磁盘状态
	protected static final SnmpOID OID_DISK_STATUS = new SnmpOID(
			".1.3.6.1.4.1.12740.3.1.1.1.8.1");
	// 磁盘大小
//	protected static final SnmpOID OID_DISK_SIZE = new SnmpOID(
//			".1.3.6.1.4.1.12740.3.1.1.1.6.1");
	// 磁盘错误数
//	protected static final SnmpOID OID_DISK_ERRORS = new SnmpOID(
//			".1.3.6.1.4.1.12740.3.1.1.1.9.1");
	// 所属 Group Member 名称
	protected static final SnmpOID OID_MEM_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.1.1.9.1");

	protected SnmpWalk walk = null;
	
	protected SnmpTarget target = null;

	Snmp snmp = new Snmp();

	public DiskInfoMgr(SnmpWalk walk) {
		this.walk = walk;
	}

	public void initSnmpTarget() {
		if (target == null) {
			target = walk.getSnmpTarget();
		}
	}

	/**
	 * Group组 成员的磁盘信息获取
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<MemberDisk> generateDiskInfo() throws Exception {
		this.initSnmpTarget();
		List<MemberDisk> diskInfos = new ArrayList();
		SnmpOID[] oids = new SnmpOID[] { OID_DISK_ID, OID_DISK_STATUS };
		SnmpTable snmpTable = snmp.walkTable(target, oids);
		Iterator itr = snmpTable.getRows().iterator();
		Map memberNames = getMemberName();
		while (itr.hasNext()) {
			MemberDisk diskInfo = new MemberDisk();
			SnmpRow row = (SnmpRow) itr.next();
			SnmpResult[] results = row.getCells();
			String extendOid = getExtendOID(results[0].getOid().toString());
			diskInfo.setDiskId(results[0].getValue().toString());
			diskInfo.setDiskStatus(convertStatus(results[1].getValue()
					.toString()));
			diskInfo.setMemberName((String) memberNames.get(extendOid
					.substring(0, extendOid.lastIndexOf("."))));
			diskInfos.add(diskInfo);
		}
		return diskInfos;
	}

	/**
	 * 获取磁盘所属成员设备 名称
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> getMemberName() throws Exception {
		Map<String, String> memberNames = new HashMap<String, String>();
		SnmpTable snmpTable = snmp.walkTable(target,
				new SnmpOID[] { OID_MEM_NAME });
		Iterator<SnmpRow> itr = snmpTable.getRows().iterator();
		while (itr.hasNext()) {
			SnmpRow row = itr.next();
			SnmpResult[] results = row.getCells();
			String oid = results[0].getOid().toString();
			String extendOid = oid
					.substring(oid.lastIndexOf("."), oid.length());
			String memberName = results[0].getValue().toString();
			memberNames.put(extendOid, memberName);
		}
		return memberNames;
	}

	/**
	 * 获取扩展oid
	 * 
	 * @return
	 * @throws SnmpException
	 */
	public String getExtendOID(String oid) throws SnmpException {
		return oid.substring(oid.lastIndexOf(".", oid.lastIndexOf(".") - 1),
				oid.length());
	}

	/**
	 * 获取磁盘的 当前状态
	 * 
	 * @return Integer on-line (1), spare (2),failed (3),off-line (4),alt-sig
	 *         (5),
	 *         too-small(6),history-of-failures(7),unsupported-version(8),unhealthy
	 *         (9),
	 *         replacement(10),encrypted(11),notApproved(12),preempt-failed(13)
	 * @throws Exception
	 */
	private String convertStatus(String status) throws Exception {
		String diskStatus = "";
		if (status != null) {
			Integer stat = Integer.parseInt(status);
			switch (stat) {
			case 1:
				diskStatus = "on-line";
				break;
			case 2:
				diskStatus = "spare";
				break;
			case 3:
				diskStatus = "failed";
				break;
			case 4:
				diskStatus = "off-line";
				break;
			case 5:
				diskStatus = "alt-sig";
				break;
			case 6:
				diskStatus = "too-small";
				break;
			case 7:
				diskStatus = "history-of-failures";
				break;
			case 8:
				diskStatus = "unsupported-version";
				break;
			case 9:
				diskStatus = "unhealthy";
				break;
			case 10:
				diskStatus = "replacement";
				break;
			case 11:
				diskStatus = "encrypted";
				break;
			case 12:
				diskStatus = "notApproved";
				break;
			case 13:
				diskStatus = "preempt-failed";
				break;
			default:
				break;
			}
		}
		return diskStatus;
	}
}
