package com.broada.carrier.monitor.impl.mw.weblogic.agent.thread;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
/**
 * 解析XML文件
 * @author zhuhong
 *
 */
public class WLSThreadMonitorUtil {

	public static List<ThreadInfo> getThreadInfomations(String _url) throws Exception{
		List<ThreadInfo> list=new ArrayList<ThreadInfo>();
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(new URL(_url));
    Element rootE = doc.getRootElement();
    ThreadInfo thread = new ThreadInfo();
    if(rootE != null){
    	Element elm = (Element) rootE.getChild("ThreadPool");
      if(elm != null){
      	thread.setInstKey("线程队列");
      	thread.setTotalThread(Double.parseDouble(elm.getAttributeValue("executeThreadTotalCount")));
      	thread.setIdleThread(Double.parseDouble(elm.getAttributeValue("executeThreadIdleCount")));
      	thread.setThroughPut(Double.parseDouble(elm.getAttributeValue("throughput")));
      	thread.setHealth(elm.getAttributeValue("health"));
      	list.add(thread);
      }
    }
    return list;
	}
}
