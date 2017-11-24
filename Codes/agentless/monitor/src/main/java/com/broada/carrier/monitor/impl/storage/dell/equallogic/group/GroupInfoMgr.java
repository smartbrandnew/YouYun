package com.broada.carrier.monitor.impl.storage.dell.equallogic.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;

/**
 * 
 * @author shoulw
 */
public class GroupInfoMgr {
	private static final Log logger = LogFactory.getLog(GroupInfoMgr.class);

	// Group组编号OID
	protected static final String OID_GRP_ID = ".1.3.6.1.4.1.12740.1.1.2.1.27.1";

	// Group名称OID
	protected static final String OID_GRP_NAME = ".1.3.6.1.4.1.12740.1.1.1.1.19.1";

	// Group组成员数OID
	protected static final String OID_GRP_MEMBERS = ".1.3.6.1.4.1.12740.1.1.2.1.13.1";

	// 正在使用的Group组成员数
	protected static final String OID_GRP_MEMBERSINUSE = ".1.3.6.1.4.1.12740.1.1.2.1.14.1";

	protected int version;

	protected String ipAddr;

	protected int port;

	protected String community;

	protected int timeout;

	protected SnmpWalk walk = null;

	public GroupInfoMgr(SnmpWalk walk) {
		this.walk = walk;
	}


	public void close() {
		if (walk != null)
			walk.close();
	}

	/**
	 * Group信息获取
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map generateHostInfo() throws Exception {

		Map groupInfo = new HashMap();
		// // Group组编号
		// groupInfo.put(GroupBaseInfo.keys[0], getGroupID());
		// Group组名称
		groupInfo.put(GroupBaseInfo.keys[0], getGroupName());
		// Group组总成员数量
		groupInfo.put(GroupBaseInfo.keys[1], getmemberCount());
		// Group组正在使用成员数量
		groupInfo.put(GroupBaseInfo.keys[2], getMembersInUse());
		return groupInfo;
	}

	// /**
	// * 获取组编号
	// *
	// * @return
	// * @throws Exception
	// */
	// private String getGroupID() throws Exception {
	// String groupId = "";
	// try {
	// SnmpValue snmpValue = getStrValueByOid(OID_GRP_ID);
	// if (snmpValue != null) {
	// groupId = snmpValue.toString();
	// }
	// } catch (SnmpException e) {
	// logger.error("获取Group组编号失败。", e);
	// throw new Exception("Group组编号获取失败，" + e.getMessage() + "。", e);
	// }
	// return groupId;
	// }

	private String getGroupName() throws Exception {
		String groupName = "";
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_GRP_NAME);
			if (snmpValue != null) {
				snmpValue.isNull();
				groupName = snmpValue.toString();
			}
		} catch (SnmpException e) {
			logger.error("获取Group组名称失败。", e);
			throw new Exception("Group组名称获取失败，" + e.getMessage() + "。", e);
		}
		return groupName;
	}

	private Double getmemberCount() throws Exception {
		double memberCount = 0;
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_GRP_MEMBERS);
			if (snmpValue != null) {
				memberCount = snmpValue.toLong();
			}
		} catch (SnmpException e) {
			logger.error("获取member总数失败。", e);
			throw new Exception("member总数获取失败，" + e.getMessage() + "。", e);
		}
		return memberCount;
	}

	private Double getMembersInUse() throws Exception {
		double membersInUse = 0;
		try {
			SnmpValue snmpValue = getStrValueByOid(OID_GRP_MEMBERSINUSE);
			if (snmpValue != null) {
				membersInUse = snmpValue.toLong();
			}
		} catch (SnmpException e) {
			logger.error("获取正在使用的member总数失败。", e);
			throw new Exception("正在使用的member总数获取失败，" + e.getMessage() + "。", e);
		}
		return membersInUse;
	}

	/**
	 * 通过OID 获取信息
	 * 
	 * @param oid
	 * @return
	 * @throws SnmpException
	 */
	protected SnmpValue getStrValueByOid(String oid) throws SnmpException {
		SnmpResult var = null;
		try {
			var = walk.snmpGet(oid);
		} catch (SnmpException e) {
			throw e;
		}
		if (var != null) {
			return var.getValue();
		} else {
			return null;
		}
	}

	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	public static String strToUnicode(String strText) throws Exception {
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else
				// 低位在前面补00
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);
			// 高位需要补上00再转
			String s1 = s.substring(2, 4) + "00";
			// 低位直接转
			String s2 = s.substring(4);
			// 将16进制的string转为int
			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
			// 将int转换为字符
			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}
}
