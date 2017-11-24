package com.broada.carrier.monitor.impl.mw.webspheremq;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.webspheremq.channel.IbmMqChannelMonitor;
import com.broada.carrier.monitor.method.webspheremq.WebSphereMQMethod;
import com.broada.carrier.monitor.method.webspheremq.WebSphereMQMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

/**
 * Created by hu on 2015/5/18.
 */
public class IbmMqMonitorPackage implements MonitorPackage {
	@Override public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "WebSphereMQ" };
		String[] methodTypeIds = new String[] { WebSphereMQMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("IBMMQ","WMQ-CHANNEL", "WebSphere MQ通道监测 [可用性]", "监测WebSphere MQ通道是否正常服务和通道的各种运行参数情况。", MultiInstanceConfiger.class.getName(),
						IbmMqChannelMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				new MonitorType("IBMMQ","WMQ-QUEUE", "WebSphere MQ队列监测", "监测WebSphere MQ队列是否正常服务和队列的各种运行参数情况。", MultiInstanceConfiger.class.getName(),
						IbmMqMonitor.class.getName(), index++, targetTypeIds, methodTypeIds)
		};
	}

	@Override public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("WMQ-CHANNEL","WMQ-CHANNEL-1", "每秒接收字节", "byte/s", "通道每秒接收字节", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-CHANNEL","WMQ-CHANNEL-2", "每秒发送字节", "byte/s", "通道每秒发送字节", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-CHANNEL","WMQ-CHANNEL-3", "通道状态", "", "通道状态", MonitorItemType.TEXT),
				new MonitorItem("WMQ-CHANNEL","WMQ-CHANNEL-4", "发送间隔", "秒", "通道发送数据的间隔", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-CHANNEL","WMQ-CHANNEL-5", "事务数", "个", "当次启动后的通道事务数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-1", "队列深度", "个", "WebSphere MQ队列的消息个数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-2", "打开输入计数", "个", "WebSphere MQ队列的打开输入计数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-3", "打开输出计数", "个", "WebSphere MQ队列的打开输出计数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-4", "消息最大字节数", "字节", "WebSphere MQ队列的消息最大字节数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-5", "放入消息", "", "WebSphere MQ队列可放入消息", MonitorItemType.TEXT),
				new MonitorItem("WMQ-QUEUE", "WMQ-QUEUE-6", "取出消息", "", "WebSphere MQ队列可取出消息", MonitorItemType.TEXT)
		};
	}

	@Override public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(WebSphereMQMethod.TYPE_ID, "IbmMq agent监测协议", WebSphereMQMethodConfiger.class) };
	}
}
