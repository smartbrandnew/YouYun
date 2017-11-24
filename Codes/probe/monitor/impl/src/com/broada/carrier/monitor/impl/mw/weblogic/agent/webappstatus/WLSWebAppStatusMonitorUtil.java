package com.broada.carrier.monitor.impl.mw.weblogic.agent.webappstatus;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * zhuhong
 */
public class WLSWebAppStatusMonitorUtil {

	public static List getWebAppInfomations(String _url) throws Exception {
		List webAppList = new ArrayList();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		if (rootE != null) {
			List webapps = rootE.getChildren("webapp");
			for (Iterator itr = webapps.iterator(); itr.hasNext();) {
				Element elm = (Element) itr.next();
				if (elm != null) {
					WebAppStatusInstance webapp = new WebAppStatusInstance();
					webapp.setAppName(elm.getAttributeValue("appName"));
					webapp.setInstanceKey(elm.getAttributeValue("instKey"));
					webapp.setDesc(webapp.getAppName());
					webapp.setAppStatus(elm.getAttributeValue("state"));
					webAppList.add(webapp);
				}
			}
		}
		return webAppList;
	}

}
