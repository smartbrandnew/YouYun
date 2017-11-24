package com.broada.carrier.monitor.impl.storage.dell.equallogic.fan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.fan.bean.MemberFan;
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
public class FanInfoMgr {
	private static final Log logger = LogFactory.getLog(FanInfoMgr.class);
	// 风扇名称
	protected static final SnmpOID OID_FAN_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.7.1.2.1");
	// 风扇状态
	protected static final SnmpOID OID_FAN_STATUS = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.7.1.4.1");
	// 所属 Group Member 名称
	protected static final SnmpOID OID_MEM_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.1.1.9.1");
	
	protected SnmpWalk walk = null;

	protected SnmpTarget target = null;

	Snmp snmp = new Snmp();

	public FanInfoMgr(SnmpWalk walk) {
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
	public List<MemberFan> generateFanInfo() throws Exception {
		this.initSnmpTarget();
		List<MemberFan> fanInfos = new ArrayList();
		SnmpOID[] oids = new SnmpOID[] { OID_FAN_NAME,
				OID_FAN_STATUS};
		SnmpTable snmpTable = snmp.walkTable(target, oids);
		Iterator itr = snmpTable.getRows().iterator();
		Map memberNames = getMemberName();
		while (itr.hasNext()) {
			MemberFan fan = new MemberFan();
			SnmpRow row = (SnmpRow) itr.next();
			SnmpResult[] results = row.getCells();
			String extendOid = getExtendOID(results[0].getOid().toString());
			fan.setFanName(results[0].getValue().toString());
			fan.setFanStatus(convertStatus(results[1].getValue().toString()));
			fan.setMemberName((String) memberNames.get(extendOid.substring(0,
					extendOid.lastIndexOf("."))));
			
			fanInfos.add(fan);
		}
		return fanInfos;
	}

	/**
	 * 获取风扇所属成员设备 名称
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
	 * 获取风扇状态
	 * 
	 * @return Integer unknown (0),normal (1), warning (2), critical (3)
	 * @throws Exception
	 */
	private String convertStatus(String status) throws Exception {
		String fanstatus = "";
		if (status != null) {
			int stat = Integer.parseInt(status);
			switch (stat) {
			case 0:
				fanstatus = "unknown";
				break;
			case 1:
				fanstatus = "normal";
				break;
			case 2:
				fanstatus = "warning";
				break;
			case 3:
				fanstatus = "critical";
				break;
			default:
				break;
			}
		}
		return fanstatus;
	}
}
