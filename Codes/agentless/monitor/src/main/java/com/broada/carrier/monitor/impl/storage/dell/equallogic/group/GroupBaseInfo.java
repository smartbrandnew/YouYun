package com.broada.carrier.monitor.impl.storage.dell.equallogic.group;

/**
 * 
 * @author broada_yaoqi
 */
public class GroupBaseInfo {
	// public static final String[] keys = new String[] { "groupID",
	// "groupName", "memberCount", "membersInUse" };
	//
	// public static final String[] colNames = new String[] { "群组ID", "群组名称",
	// "群组成员总数", "正在使用的成员数" };
	//
	// public static final String[] descs = new String[] {
	// "EqualLogic Group 组的编号。", "EqualLogic Group 组的名称。",
	// "EqualLogic Group 组内加入的存储设备的数量。", "EqualLogic Group 组内加入的存储设备中正在使用的数量。"
	// };
	public static final String[] keys = new String[] { "groupName",
			"memberCount", "membersInUse" };

	public static final String[] colNames = new String[] { "群组名称", "群组成员总数",
			"正在使用的成员数" };

	public static final String[] descs = new String[] {
			"EqualLogic Group 组的名称。", "EqualLogic Group 组内加入的存储设备的数量。",
			"EqualLogic Group 组内加入的存储设备中正在使用的数量。" };
}
