package com.broada.carrier.monitor.impl.mw.weblogic.agent.jdbc;

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
public class WLSJdbcMonitorUtil {

	public static List<JdbcInfo> getJdbcInfomations(String _url) throws Exception {
		List<JdbcInfo> jdbcList = new ArrayList<JdbcInfo>();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		if (rootE != null) {
			List jdbcs = rootE.getChildren("JDBCConnection");
			for (Iterator itr = jdbcs.iterator(); itr.hasNext();) {
				Element elm = (Element) itr.next();
				if (elm != null) {
					JdbcInfo jdbc = new JdbcInfo();
					jdbc.setJdbcName(elm.getAttributeValue("instKey"));
					jdbc.setAvgCount(Integer.parseInt(elm.getAttributeValue("averageCount")));
					jdbc.setCurrCount(Integer.parseInt(elm.getAttributeValue("currentCount")));
					jdbc.setHighCount(Integer.parseInt(elm.getAttributeValue("highCount")));
					jdbc.setStatus(elm.getAttributeValue("state"));
					jdbcList.add(jdbc);
				}
			}
		}
		return jdbcList;
	} 

}
