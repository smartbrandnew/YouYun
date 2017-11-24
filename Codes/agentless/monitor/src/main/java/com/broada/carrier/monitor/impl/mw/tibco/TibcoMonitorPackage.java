package com.broada.carrier.monitor.impl.mw.tibco;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.tibco.packets.PacketsMonitor;
import com.broada.carrier.monitor.impl.mw.tibco.subject.TibcoSbjMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpBaseConfigPanel;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.tomcat.TomcatMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class TibcoMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Tibco" };
		String[] methodTypeIds = new String[] { SnmpMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("TIBCO", "TIBCOPACKETS", "TIBRV数据包监测", "监测Tibco Rendezvous数据包的相关指标,超出设定的阈值便会发出告警。",
						SingleInstanceConfiger.class.getName(), PacketsMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("TIBCO", "TIBCOSUBJECT", "TIBRV主题消息监测", "监测Tibco Rendezvous的主题(Subject)的消息个数，超出阈值告警。",
						SingleInstanceConfiger.class.getName(), TibcoSbjMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("TIBCOPACKETS","TIBCOPACKETS-1", "多播/广播错包率", "%", "多播/广播错包率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS","TIBCOPACKETS-2", "多播/广播丢包率", "%", "多播/广播丢包率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS", "TIBCOPACKETS-3", "多播/广播无效包率", "%", "多播/广播无效包率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS", "TIBCOPACKETS-4", "重传包中被拒绝的比率", "%", "重传包中被拒绝的比率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS", "TIBCOPACKETS-5", "重传包中传输错误比率", "%", "重传包中传输错误比率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS", "TIBCOPACKETS-6", "点对点传输错包率", "%", "点对点传输错包率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOPACKETS", "TIBCOPACKETS-7", "点对点传输包被拒绝的比率", "%", "点对点传输包被拒绝的比率", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOSUBJECT","TIBCOSUBJECT-1", "主题消息个数", "个", "主题(Subject)消息个数", MonitorItemType.NUMBER),
				new MonitorItem("TIBCOSUBJECT","TIBCOSUBJECT-2", "主题消息字节数", "字节", "主题(Subject)消息字节数", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(TomcatMonitorMethodOption.TYPE_ID, "Tibco snmp监测协议",
				SnmpBaseConfigPanel.class) };
	}
}
