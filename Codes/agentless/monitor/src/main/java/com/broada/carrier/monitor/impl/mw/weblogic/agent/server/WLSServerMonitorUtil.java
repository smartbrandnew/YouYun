package com.broada.carrier.monitor.impl.mw.weblogic.agent.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * 
 * server性能检测的数据获取工具类
 * 
 * @author Yaojj Create By Mar 25, 2010 5:05:29 PM
 */
public class WLSServerMonitorUtil {

	public static List<ServerPerfInst> getServerPerfInfomations(String _url) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		List<ServerPerfInst> list = new ArrayList<ServerPerfInst>();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		ServerPerfInst serverPerf = new ServerPerfInst();
		if (rootE != null) {
			List propsList = rootE.getChildren("property");

			for (Iterator itr = propsList.iterator(); itr.hasNext();) {
				Element elm = (Element) itr.next();
				if (elm != null) {
					if (elm.getAttributeValue("name").equals("memoryUsage")) {
						serverPerf.setMemoryUsage(Double.parseDouble(elm.getAttributeValue("value")));
					} else if (elm.getAttributeValue("name").equals("state")) {
						serverPerf.setState(elm.getAttributeValue("value"));
					} else if (elm.getAttributeValue("name").equals("healthState")) {
						serverPerf.setHealthState(elm.getAttributeValue("value"));
					} else if (elm.getAttributeValue("name").equals("executeThreadCurrentIdleCount")) {
						serverPerf.setExecuteThreadCurrentIdleCount(Integer.parseInt((elm.getAttributeValue("value"))));
					} else if (elm.getAttributeValue("name").equals("pendingRequestCurrentCount")) {
						serverPerf.setPendingRequestCurrentCount(Integer.parseInt((elm.getAttributeValue("value"))));
					} else if (elm.getAttributeValue("name").equals("name")) {
						serverPerf.setServerName(elm.getAttributeValue("value"));
						serverPerf.setInstKey(elm.getAttributeValue("value"));
					}

				}

			}
		}
		list.add(serverPerf);
		return list;
	}
}
