package com.broada.carrier.monitor.impl.mw.weblogic.agent.jvm;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * 解析XML文件
 * 
 * @author zhuhong
 * 
 */
public class WLSJvmMonitorUtil {

	public static List<JvmInfo> getJvmInfomations(String _url) throws Exception {
		List<JvmInfo> list = new ArrayList<JvmInfo>();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		JvmInfo jvm = new JvmInfo();
		if (rootE != null) {
			List jvmList = rootE.getChildren("property");
			jvm.setInstKey("java虚拟机");
			for (Iterator itr = jvmList.iterator(); itr.hasNext();) {
				Element elm = (Element) itr.next();
				if (elm != null) {
					if (elm.getAttributeValue("name").equals("heapSizeCurrent")) {
						jvm.setHeapCurr(elm.getAttributeValue("value"));
						continue;
					}
					if (elm.getAttributeValue("name").equals("heapFreeCurrent")) {
						jvm.setHeapFree(elm.getAttributeValue("value"));
						continue;
					}
					if (elm.getAttributeValue("name").equals("heapSizeMax")) {
						jvm.setHeapMax(elm.getAttributeValue("value"));
						continue;
					}
					if (elm.getAttributeValue("name").equals("heapFreePercent")) {
						jvm.setHeapPercent(Double.parseDouble((elm.getAttributeValue("value"))));
						continue;
					}

				}

			}
		}
		list.add(jvm);
		return list;
	}

}
