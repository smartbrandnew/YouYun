package com.broada.carrier.monitor.impl.mw.weblogic.agent.ejb;

import java.io.File;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;

import com.broada.carrier.monitor.impl.mw.weblogic.agent.WLSRemoteException;
import com.broada.utils.StringUtil;

public class WLSEJBMonitorUtil {
  private static final String EJB_DIGESTER_RULE = "conf/wls/ejb-digester-rule.xml";

  public static EJBCollection getEjbCollection(String _url) throws Exception {
    Digester digester = DigesterLoader.createDigester(new File(EJB_DIGESTER_RULE).toURI().toURL());
    digester.setValidating(false);
    URL url = new URL(_url);
    
    EJBCollection ejbCollection = (EJBCollection) digester.parse(url.openStream());
    if(!StringUtil.isNullOrBlank(ejbCollection.getMessage()) || !StringUtil.isNullOrBlank(ejbCollection.getDetail())){
      throw new WLSRemoteException(ejbCollection.getMessage(), ejbCollection.getDetail());
    }
    return ejbCollection;
  }
}
