package com.broada.carrier.monitor.impl.mw.weblogic.agent.servlet;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 解析XML文件
 * 
 * @author zhuhong
 * 
 */
public class WLSServletMonitorUtil {

	public static List<ServletInstances> getServletInfomations(String _url) throws Exception {
		List<ServletInstances> servletList = new ArrayList<ServletInstances>();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		if (rootE != null) {
			List servlets = rootE.getChildren("servlet");
			for (Iterator itr = servlets.iterator(); itr.hasNext();) {
				Element elm = (Element) itr.next();
				if (elm != null) {
					ServletInstances servlet = new ServletInstances();
					servlet.setInstKey(elm.getAttributeValue("instKey"));
					servlet.setServletName(elm.getAttributeValue("servletName"));
					servlet.setInvokeTimes(elm.getAttributeValue("reloadTotalCount"));
					servlet.setMaxTime(Double.parseDouble(elm.getAttributeValue("executionTimeHigh")));
					servlet.setAvgTime(Double.parseDouble(elm.getAttributeValue("executionTimeAverage")));
					servletList.add(servlet);
				}
			}
		}
		return servletList;
	}

}
