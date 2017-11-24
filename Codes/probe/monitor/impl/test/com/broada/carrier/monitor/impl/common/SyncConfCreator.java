package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;

public class SyncConfCreator {
	public static void main(String[] args) {
		MonitorItem[] items = new MonitorItem[] {
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-1", "队列深度", "个", "WebSphere MQ队列的消息个数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-2", "打开输入计数", "个", "WebSphere MQ队列的打开输入计数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-3", "打开输出计数", "个", "WebSphere MQ队列的打开输出计数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-4", "消息最大字节数", "字节", "WebSphere MQ队列的消息最大字节数", MonitorItemType.NUMBER),
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-5", "放入消息", "", "WebSphere MQ队列可放入消息", MonitorItemType.TEXT),
				new MonitorItem("WMQ-QUEUE","WMQ-QUEUE-6", "取出消息", "", "WebSphere MQ队列可取出消息", MonitorItemType.TEXT)
		};
		String type = "WMQ-QUEUE";
		String perfGroup = "was-mq-queue";
		String remote = "WebSphereMQ";
		boolean useScript = true;
		
		System.out.println("	<monitor type=\"" + type + "\" >");
		System.out.println("		<!-- ");
		for (MonitorItem item : items) {
			System.out.println("		" + item.getCode() + "\t" + item.getName() + "\t" + item.getUnit());
		}
		System.out.println("		 -->");
		System.out.println("		<object local=\"resource\" remote=\"" +remote + "\">");
		if (useScript) {
			System.out.println("		<![CDATA[");
			for (MonitorItem item : items) {
				//System.out.println("			output.setValue(input, \"perf." + perfGroup + ".\", input.instance.getIndicator(\"" + item.getCode() + "\"));");
				System.out.println("			util.setOutputValue(\"" + item.getCode() + "\", \"sum\", \"perf." + perfGroup + ".\");");
			}
			System.out.println("		]]>");
		} else  {
			for (MonitorItem item : items) {
				System.out.println("			<item local=\"perf." + item.getCode() + "\" remote=\"perf." + perfGroup + ".\" />");
			}
		}
		System.out.println("		</object>"); 
		System.out.println("	</monitor>");
	}
}
