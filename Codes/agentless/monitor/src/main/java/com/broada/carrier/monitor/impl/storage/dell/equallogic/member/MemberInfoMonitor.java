package com.broada.carrier.monitor.impl.storage.dell.equallogic.member;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.member.bean.GroupMember;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class MemberInfoMonitor extends BaseMonitor{
	
	private static final Log logger = LogFactory
	.getLog(MemberInfoMonitor.class);
// 性能项的个数

/**
* 实现doMonitor 监测器采集数据 的实现方法
*/

@Override
public Serializable collect(CollectContext context) {
	MonitorResult result = new MonitorResult();
	SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
	SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
	List<GroupMember> memberInfoList;
	MemberInfoMgr memmgr = new MemberInfoMgr(walk);
	try {
		MemberInfoMgr mgr = new MemberInfoMgr(walk);
		try {
			memberInfoList = mgr.generateMemberInfo();
		} catch (Exception e) {
			return null;
		}
		 
		 for (GroupMember memberInfo : memberInfoList) {		
				MonitorResultRow row = new MonitorResultRow();
				row.setInstCode(memberInfo.getMemberName());
				row.setInstName(memberInfo.getMemberName());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-1", memberInfo.getMemberName());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-2", memberInfo.getMemberState());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-3", memberInfo.getMemberInfoState());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-4", memberInfo.getMemberModel());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-5", memberInfo.getMemberSeriesNumber());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-6", memberInfo.getMemberDiskNumber());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-7", memberInfo.getMemberTotalStorage());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-8", memberInfo.getMemberUsedStorage());
				row.setIndicator("DELLEQUALLOGIC-MEMBER-INFO-9", memberInfo.getDriveGroupRAIDPolicy());
				result.addRow(row);
			}
	
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		walk.close();
	}
	return result;
}

}
