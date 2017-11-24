package com.broada.carrier.monitor.impl.mw.tomcat.basic;

import com.broada.carrier.monitor.impl.mw.tomcat.AbstractTomcatManager;
import com.broada.carrier.monitor.impl.mw.tomcat.Tomcat;

import org.apache.commons.beanutils.BeanMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TomcatBasicManager extends AbstractTomcatManager {

	public final static String SERVER_INFO_START = "Server Information";

	public final static String SERVER_INFO_SPLITER_START = "<small>";

	public final static String SERVER_INFO_SPLITER_END = "</small>";

	private static Map<String, String> MAPINFO = new HashMap<String, String>();
	static {
		MAPINFO.put("Tomcat Version", "tomcatVersion");
		MAPINFO.put("JVM Version", "jvmVersion");
		MAPINFO.put("JVM Vendor", "jvmVendor");
		MAPINFO.put("OS Name", "osName");
		MAPINFO.put("OS Version", "osVersion");
		MAPINFO.put("OS Architecture", "osArchitecture");
	}

	public Tomcat fetchInfo(InputStream is) throws IOException {
		List<String> serverInfoList = new ArrayList<String>();
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
		for (String line = ""; line != null; line = bufferedreader.readLine()) {
			if (line.indexOf(SERVER_INFO_START) > -1) {
				int i = 0;
				while (i <= 25) {
					line = bufferedreader.readLine();
					if (line.contains(SERVER_INFO_SPLITER_START)) {
						serverInfoList.add(line.substring(line.indexOf(SERVER_INFO_SPLITER_START)
									+ SERVER_INFO_SPLITER_START.length(), line.indexOf(SERVER_INFO_SPLITER_END)));
					}
					i++;
				}
				break;
			}
		}

		bufferedreader.close();
		is.close();

		TomcatBasic tb = new TomcatBasic();
		BeanMap bm = new BeanMap(tb);
		for (int i = 0, len = serverInfoList.size() / 2; i < len; i++) {
			if (MAPINFO.get(serverInfoList.get(i)) != null)
				bm.put(MAPINFO.get(serverInfoList.get(i)), serverInfoList.get(i + len));

		}

		return (Tomcat) bm.getBean();
	}

}
