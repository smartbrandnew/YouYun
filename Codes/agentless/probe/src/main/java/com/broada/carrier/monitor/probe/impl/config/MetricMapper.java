package com.broada.carrier.monitor.probe.impl.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import com.broada.carrier.monitor.probe.impl.entity.MetricType;
import com.broada.carrier.monitor.probe.impl.entity.RemoteMapper;
import com.broada.carrier.monitor.probe.impl.util.XmlUtil;

public class MetricMapper {
	private static MetricMapper instance = new MetricMapper();
	private static Map<String, RemoteMapper> metricMap = new HashMap<String, RemoteMapper>();

	public static MetricMapper getInstance() {
		return instance;
	}

	static {
		String path = Config.getConfDir() + "/mapper.xml";
		Element element = XmlUtil.getXMLRoot(path);
		@SuppressWarnings("unchecked")
		List<Element> list = element.getChildren("metrics");
		for (Element ele : list) {
			@SuppressWarnings("unchecked")
			List<Element> elements = ele.getChildren("itemcode");
			for (Element el : elements) {
				String typeId = el.getAttributeValue("name").toLowerCase();
				@SuppressWarnings("unchecked")
				List<Element> list1 = el.getChildren("property");
				for (Element e : list1) {
					String name = e.getAttributeValue("name");
					String remoteName = e.getAttributeValue("remotename");
					int val = Integer.parseInt(e.getAttributeValue("value_type"));
					if (val == 0 || val == 1)
						metricMap.put(typeId + "." + name, new RemoteMapper(remoteName, MetricType.checkByIndex(val)));
				}

			}
		}

	}

	public RemoteMapper getRemoteMetricType(String typeId, String name) {
		return metricMap.get(typeId.toLowerCase() + "." + name);
	}

}
