package com.broada.carrier.monitor.impl.mw.weblogic.agent.subsystem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class WLSSubSystemMonitorUtil {

	public static List<SubSystemInstance> getSubSystemInfo(String url) throws MalformedURLException, JDOMException,
			IOException {
		SAXBuilder builder = new SAXBuilder();
		List<SubSystemInstance> list = new ArrayList<SubSystemInstance>();
		Document doc = builder.build(new URL(url));
		Element rootE = doc.getRootElement();
		if (rootE != null) {
			Element e = rootE.getChild("SystemHealth");
			if (e != null) {
				List children = e.getChildren("subSystem");
				if (children != null) {
					for (Iterator iterator = children.iterator(); iterator.hasNext();) {
						Element element = (Element) iterator.next();
						if (element != null) {
							SubSystemInstance subSystem = new SubSystemInstance();
							subSystem.setSubSystem(element.getAttributeValue("subSystem"));
							subSystem.setState(element.getAttributeValue("state"));
							subSystem.setReasonCode(element.getAttributeValue("reasonCode"));
							subSystem.setInstKey(element.getAttributeValue("subSystem"));
							list.add(subSystem);
						}
					}
				}
			}
		}
		return list;
	}
}
