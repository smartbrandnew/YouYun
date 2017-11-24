package com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.procTemp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.bean.MemberController;
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
public class ControllerInfoMgr {
	private static final Log logger = LogFactory
			.getLog(ControllerInfoMgr.class);
	// 控制器序列号
	protected static final SnmpOID OID_CONTR_SERIALNUM = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.33.1");
	// 控制器版本号
	protected static final SnmpOID OID_CONTR_REVISION = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.3.1");
	// 主控制器或副控制器
	protected static final SnmpOID OID_CONTR_PRIMORSEC = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.9.1");
	// 控制器类型
	protected static final SnmpOID OID_CONTR_TYPE = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.34.1");
	// 控制器启动时间
//	protected static final SnmpOID OID_CONTR_BOOTTIME = new SnmpOID(
//			".1.3.6.1.4.1.12740.4.1.1.1.35.1");
	// 处理器温度
	protected static final SnmpOID OID_CONTR_PROCTEMP = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.7.1");
	// 芯片温度
//	protected static final SnmpOID OID_CONTR_CHIPTEMP = new SnmpOID(
//			".1.3.6.1.4.1.12740.4.1.1.1.8.1");
	// 电池状态
	protected static final SnmpOID OID_CONTR_BATTERYSTATUS = new SnmpOID(
			".1.3.6.1.4.1.12740.4.1.1.1.5.1");
	// 物理内存大小
//	protected static final SnmpOID OID_CONTR_PHYSRAM = new SnmpOID(
//			".1.3.6.1.4.1.12740.4.1.1.1.22.1");
	// 所属 Group Member 名称
	protected static final SnmpOID OID_MEM_NAME = new SnmpOID(
			".1.3.6.1.4.1.12740.2.1.1.1.9.1");


	protected SnmpWalk walk = null;

	protected SnmpTarget target = null;

	Snmp snmp = new Snmp();

	public ControllerInfoMgr(SnmpWalk walk) {
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
	public List<MemberController> generateControllerInfo() throws Exception {
		this.initSnmpTarget();
		List<MemberController> controllerInfos = new ArrayList();
		SnmpOID[] oids = new SnmpOID[] { OID_CONTR_SERIALNUM,
				OID_CONTR_REVISION, OID_CONTR_PRIMORSEC, OID_CONTR_TYPE,
			    OID_CONTR_PROCTEMP, OID_CONTR_BATTERYSTATUS};
		SnmpTable snmpTable = snmp.walkTable(target, oids);
		Iterator itr = snmpTable.getRows().iterator();
		Map memberNames = getMemberName();
		while (itr.hasNext()) {
			MemberController controllerInfo = new MemberController();
			SnmpRow row = (SnmpRow) itr.next();
			SnmpResult[] results = row.getCells();
			String extendOid = getExtendOID(results[0].getOid().toString());
			controllerInfo.setSerialNumber(results[0].getValue().toString());
			controllerInfo.setContrRevision(results[1].getValue().toString());
			controllerInfo.setContrPrimOrSec(convertPrimOrSec(results[2]
					.getValue().toString()));
			controllerInfo.setContrType(results[3].getValue().toString());
//			controllerInfo.setContrBootTime(convertDate(results[4].getValue()
//					.toLong()));
			controllerInfo.setProcessorTemp((double) results[4].getValue()
					.toLong());
//			controllerInfo.setChipsetTemp((double) results[5].getValue()
//					.toLong());
			controllerInfo.setBatteryStatus(results[5].getValue().toString());
//			controllerInfo.setContrPhysRam(new BigDecimal(results[7].getValue()
//					.toLong() / 1024.0).setScale(2, BigDecimal.ROUND_HALF_UP)
//					.doubleValue());
			controllerInfo.setMemberName((String) memberNames.get(extendOid
					.substring(0, extendOid.lastIndexOf("."))));
			controllerInfos.add(controllerInfo);
		}
		return controllerInfos;
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
	 * 控制器是主控制器还是副控制器
	 * 
	 * @return primary (1),secondary (2)
	 * @throws Exception
	 */
	private String convertPrimOrSec(String primOrSec) throws Exception {
		String result = "";
		if (primOrSec != null) {
			int temp = Integer.parseInt(primOrSec);
			switch (temp) {
			case 1:
				result = "primary";
				break;
			case 2:
				result = "secondary";
				break;
			default:
				break;
			}
		}
		return result;
	}

	public String convertDate(Long time) {
		String date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = sdf.format(new Date(time));
		return date;
	}
}
