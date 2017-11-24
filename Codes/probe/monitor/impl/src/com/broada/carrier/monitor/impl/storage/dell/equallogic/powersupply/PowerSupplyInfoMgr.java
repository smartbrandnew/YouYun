package com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply.bean.MemberPowerSupply;
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
 * @author broada_liucw
 */
public class PowerSupplyInfoMgr {
	private static final Log logger = LogFactory
			.getLog(PowerSupplyInfoMgr.class);
	// 电源名称
	protected static final SnmpOID OID_POWER_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.8.1.2.1");
	// 电源状态
	protected static final SnmpOID OID_POWER_STATUS = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.8.1.3.1");
	// 电源风扇状态
//	protected static final SnmpOID OID_POWER_FANSTATUS = new SnmpOID(
//			".1.3.6.1.4.1.12740.2.1.8.1.4.1");
	// 所属 Group Member 名称
	protected static final SnmpOID OID_MEM_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.1.1.9.1");

	protected SnmpWalk walk = null;
	
	protected SnmpTarget target = null;
	
	Snmp snmp = new Snmp();

	public PowerSupplyInfoMgr(SnmpWalk walk) {
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
	public List<MemberPowerSupply> generatePowerSupplyInfo() throws Exception {
		this.initSnmpTarget();
		List<MemberPowerSupply> powerSupplyInfos = new ArrayList();
		SnmpOID[] oids = new SnmpOID[] { OID_POWER_NAME, OID_POWER_STATUS};
		SnmpTable snmpTable = snmp.walkTable(target, oids);
		Iterator itr = snmpTable.getRows().iterator();
		Map memberNames = getMemberName();
		while (itr.hasNext()) {
			MemberPowerSupply powerSupply = new MemberPowerSupply();
			SnmpRow row = (SnmpRow) itr.next();
			SnmpResult[] results = row.getCells();
			String extendOid = getExtendOID(results[0].getOid().toString());
			powerSupply.setPowerSupplyName(results[0].getValue().toString());
			powerSupply.setPowerSupplyStatus(convertStatus(results[1]
					.getValue().toString()));
//			powerSupply.setPowerSupplyFanStatus(convertFanStatus(results[2]
//					.getValue().toString()));
			powerSupply.setMemberName((String) memberNames.get(extendOid
					.substring(0, extendOid.lastIndexOf("."))));
			powerSupplyInfos.add(powerSupply);
		}
		return powerSupplyInfos;
	}

	/**
	 * 获取电源所属成员设备 名称
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
	 * 获取电源状态
	 * 
	 * @return Integer on-and-operating (1),no-ac-power (2),failed-or-no-data
	 *         (3)
	 * @throws Exception
	 */
	private String convertStatus(String status) throws Exception {
		String powerSupplystatus = "";
		if (status != null) {
			int stat = Integer.parseInt(status);
			switch (stat) {
			case 1:
				powerSupplystatus = "on-and-operating";
				break;
			case 2:
				powerSupplystatus = "no-ac-power";
				break;
			case 3:
				powerSupplystatus = "failed-or-no-data";
				break;
			default:
				break;
			}
		}
		return powerSupplystatus;
	}

	/**
	 * 获取电源风扇状态
	 * 
	 * @return Integer not-applicable (0),fan-is-operational
	 *         (1),fan-not-operational (2)
	 * @throws Exception
	 */
//	private String convertFanStatus(String status) throws Exception {
//		String powerSupplyFanstatus = "";
//		if (status != null) {
//			int stat = Integer.parseInt(status);
//			switch (stat) {
//			case 0:
//				powerSupplyFanstatus = "not-applicable";
//				break;
//			case 1:
//				powerSupplyFanstatus = "fan-is-operational";
//				break;
//			case 2:
//				powerSupplyFanstatus = "fan-not-operational";
//				break;
//			default:
//				break;
//			}
//		}
//		return powerSupplyFanstatus;
//	}
}
