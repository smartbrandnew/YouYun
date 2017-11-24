package com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.bean.MemberIPConf;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpRow;
import com.broada.snmputil.SnmpTable;
import com.broada.snmputil.SnmpTarget;

;

/**
 * 
 * @author broada_liucw
 */
public class IPConfInfoMgr {
	private static final Log logger = LogFactory.getLog(IPConfInfoMgr.class);
	// 网络接口编号
	protected static final SnmpOID OID_IPCONF_INDEX = new SnmpOID(
			".1.3.6.1.4.1.12740.9.1.1.4.1");
	// 网络接口名称
	protected static final SnmpOID OID_IPCONF_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.9.1.1.2.1");
	// 子网掩码
	protected static final SnmpOID OID_IPCONF_MASK = new SnmpOID(
			".1.3.6.1.4.1.12740.9.1.1.3.1");
	// 网络接口状态
	protected static final SnmpOID OID_IPCONF_STATUS = new SnmpOID(
			".1.3.6.1.4.1.12740.9.2.1.3.1");
	// 所属 Group Member 名称
	protected static final SnmpOID OID_MEM_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.1.1.9.1");

	protected SnmpWalk walk = null;

	protected SnmpTarget target = null;

	Snmp snmp = new Snmp();

	public IPConfInfoMgr(SnmpWalk walk) {
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
	public List<MemberIPConf> generateIPConfInfo() throws Exception {
		this.initSnmpTarget();
		List<MemberIPConf> ipConfInfos = new ArrayList();
		SnmpOID[] oids = new SnmpOID[] { OID_IPCONF_INDEX, OID_IPCONF_NAME,
				OID_IPCONF_MASK };
		SnmpTable snmpTable = snmp.walkTable(target, oids);
		Iterator itr = snmpTable.getRows().iterator();
		Map memberNames = getMemberName();
		Map statuss = getStatus();
		while (itr.hasNext()) {
			MemberIPConf ipConf = new MemberIPConf();
			SnmpRow row = (SnmpRow) itr.next();
			SnmpResult[] results = row.getCells();
			String extendOid = getExtendOID(results[0].getOid().toString());
			ipConf.setInterfaceIndex(results[0].getValue().toString());
			ipConf.setInterfaceName(results[1].getValue().toString());
			ipConf.setIpAddress(extendOid.substring(extendOid.indexOf(".") + 1,
					extendOid.length()));
			ipConf.setSubNetMask(results[2].getValue().toString());
			ipConf.setStatus((String) statuss.get(extendOid.substring(0,
					extendOid.indexOf("."))
					+ "."
					+ results[0].getValue().toString()));
			ipConf.setMemberName((String) memberNames.get("."
					+ extendOid.substring(0, extendOid.indexOf("."))));
			ipConfInfos.add(ipConf);
		}
		return ipConfInfos;
	}

	/**
	 * 获取网络接口所属成员设备 名称
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
	 * 获取网络接口 的状态
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> getStatus() throws Exception {
		Map<String, String> statuss = new HashMap<String, String>();
		SnmpTable snmpTable = snmp.walkTable(target,
				new SnmpOID[] { OID_IPCONF_STATUS });
		Iterator<SnmpRow> itr = snmpTable.getRows().iterator();
		while (itr.hasNext()) {
			SnmpRow row = itr.next();
			SnmpResult[] results = row.getCells();
			String oid = results[0].getOid().toString();
			String extendOid = oid.substring(
					oid.lastIndexOf(".", oid.lastIndexOf(".") - 1) + 1,
					oid.length());
			String status = convertStatus(results[0].getValue().toString());
			statuss.put(extendOid, status);
		}
		return statuss;
	}

	/**
	 * 获取扩展oid
	 * 
	 * @return
	 * @throws SnmpException
	 */
	public String getExtendOID(String oid) throws SnmpException {
		String str = oid.replace(".", "#");
		String[] strs = str.split("#");
		int length = strs.length;
		String extoid = strs[length - 5] + "." + strs[length - 4] + "."
				+ strs[length - 3] + "." + strs[length - 2] + "."
				+ strs[length - 1];
		return extoid;
	}

	/**
	 * 转换网络接口的状态
	 * 
	 * @return Integer up(1), down(2),testing(3)
	 * @throws Exception
	 */
	private String convertStatus(String status) throws Exception {
		String ipConfstatus = "";
		if (status != null) {
			int stat = Integer.parseInt(status);
			switch (stat) {
			case 1:
				ipConfstatus = "up";
				break;
			case 2:
				ipConfstatus = "down";
				break;
			case 3:
				ipConfstatus = "testing";
				break;
			default:
				break;
			}
		}
		return ipConfstatus;
	}
}
