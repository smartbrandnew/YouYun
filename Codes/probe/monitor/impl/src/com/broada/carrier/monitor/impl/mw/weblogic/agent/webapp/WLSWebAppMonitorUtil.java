package com.broada.carrier.monitor.impl.mw.weblogic.agent.webapp;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.broada.carrier.monitor.impl.mw.weblogic.entity.WebAppInstance;

/**
 * <p>Title: WLSWebAppMonitorUtil</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4.2
 */
public class WLSWebAppMonitorUtil {

  public static List<WebAppInstance> getWebAppInfomations(String _url) throws Exception{
    List<WebAppInstance> webAppList = new ArrayList<WebAppInstance>();
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(new URL(_url));
    Element rootE = doc.getRootElement();
    if(rootE != null){
      List webapps = rootE.getChildren("webapp");
      for(Iterator itr = webapps.iterator(); itr.hasNext();){
        Element elm = (Element) itr.next();
        if(elm != null){
          WebAppInstance webapp = new WebAppInstance();
          webapp.setAppName(elm.getAttributeValue("appName"));
          webapp.setInstanceKey(elm.getAttributeValue("instKey"));
          webapp.setDesc(webapp.getAppName());
          webapp.setCurSession(new Long(elm.getAttributeValue("curSession")));
          webapp.setCurMaxSession(new Long(elm.getAttributeValue("maxSession")));
          webapp.setCurTotalSession(new Long(elm.getAttributeValue("totalSession")));
          webAppList.add(webapp);
        }
      }
    }
    return webAppList;
  }
  
}
