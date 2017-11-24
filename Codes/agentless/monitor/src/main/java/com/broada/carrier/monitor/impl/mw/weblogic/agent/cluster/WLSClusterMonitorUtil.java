package com.broada.carrier.monitor.impl.mw.weblogic.agent.cluster;

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

/**
 * 
 * weblogic 集群监测的工具类
 * 
 * @author Yaojj Create By Mar 18, 2010 11:39:43 AM
 */
public class WLSClusterMonitorUtil {

//	public static ClusterInstance getClusterInformations(String _url,String clusterName) throws MalformedURLException, JDOMException,
//			IOException {
//		SAXBuilder builder = new SAXBuilder();
//		Document doc = builder.build(new URL(_url));
//		Element rootE = doc.getRootElement();
//		if (rootE != null) {
//			ClusterInstance inst = new ClusterInstance();
//			List clusterList = rootE.getChildren("cluster");
//			for (Iterator iterator = clusterList.iterator(); iterator.hasNext();) {
//				Element cluster = (Element) iterator.next();
//				if (cluster != null && cluster.getAttributeValue("clusterName").equalsIgnoreCase(clusterName)) {
//					inst.setClusterName(cluster.getAttributeValue("clusterName"));
//					Element servers = cluster.getChild("servers");
//					List serverList = servers.getChildren("server");
//					List<ServerInstance> serverInstances = new ArrayList<ServerInstance>();
//					for (Iterator iterator2 = serverList.iterator(); iterator2.hasNext();) {
//						Element serv = (Element) iterator2.next();
//						ServerInstance serverInstance = new ServerInstance();
//						if (serv != null) {
//							serverInstance.setServerName(serv.getAttributeValue("name"));
//							serverInstance.setState(serv.getAttributeValue("state"));
//							serverInstances.add(serverInstance);
//						}
//					}
//					inst.setServers(serverInstances);
//					break;
//				}
//			}
//			return inst;
//		}else{
//			return null;
//		}
//		
//	}
	
	public static List getClusterInformations(String _url) throws MalformedURLException, JDOMException, IOException{
		List list = new ArrayList();
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new URL(_url));
		Element rootE = doc.getRootElement();
		if (rootE != null) {
			List clusterList = rootE.getChildren("cluster");
			for (Iterator iterator = clusterList.iterator(); iterator.hasNext();) {
				Element cluster = (Element) iterator.next();
				if (cluster != null ) {
					String clusterName = cluster.getAttributeValue("clusterName");
					Element servers = cluster.getChild("servers");
					List serverList = servers.getChildren("server");
					for (Iterator iterator2 = serverList.iterator(); iterator2.hasNext();) {
						Element serv = (Element) iterator2.next();
						ServerInstance serverInstance = new ServerInstance();
						if (serv != null) {
							serverInstance.setServerName(serv.getAttributeValue("name"));
							serverInstance.setState(serv.getAttributeValue("state"));
							serverInstance.setClusterName(clusterName);
							list.add(serverInstance);
						}
					}
				}
			}
		}
		return list;
	}
}
