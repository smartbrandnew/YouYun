package com.broada.carrier.monitor.impl.mw.jboss.thread;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;

public class Jboss6ThreadMonitorUtil {
	public static Jboss6ThreadInformation getJboss6ThreadInfo(JbossJMXOption jbossjmxoption) throws HttpException,
			IOException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		String url = "http://"
				+ host
				+ ":"
				+ port
				+ "/jmx-console/HtmlAdaptor?action=inspectMBean&name=jboss.threads%3Atype%3DboundedQueueThreadPool%2Cname%3DThreadPool";
		int coreThreads = 0;
		int rejectedCount = 0;
		int currentThreadCount = 0;
		int maxThreads = 0;
		GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
		String str = method.getResponseBodyAsString();
		method.releaseConnection();
		Document doc = Jsoup.parse(str);

		Elements formInfos = doc.select("form");
		Element tableInfos = formInfos.get(0);
		Elements trInfo = tableInfos.select("tr");
		trInfo.remove(0);
		trInfo.remove(trInfo.size() - 1);
		for (Element info : trInfo) {
			Element param = info.child(0);
			String paramName = param.ownText().trim();
			Element tdInfo = info.child(4);
			Element paramInfo = tdInfo.child(0);
			String paramValue = paramInfo.ownText().trim();
			if (paramInfo.getAllElements().size() != 1)
				paramValue = paramInfo.child(0).val().trim();
			if ("CoreThreads".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				coreThreads = Integer.parseInt(paramValue);
			} else if ("MaxThreads".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				maxThreads = Integer.parseInt(paramValue);
			} else if ("RejectedCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				rejectedCount = Integer.parseInt(paramValue);
			} else if ("CurrentThreadCount".equalsIgnoreCase(paramName) && paramValue != null && !"".equals(paramValue)) {
				currentThreadCount = Integer.parseInt(paramValue);
			}
		}
		String name = "default";
		Jboss6ThreadInformation info = new Jboss6ThreadInformation();
		info.setCoreThreads(coreThreads);
		info.setCurrentThreadCount(currentThreadCount);
		info.setMaxThreads(maxThreads);
		info.setRejectedCount(rejectedCount);
		info.setName(name);
		return info;
	}
}
