package com.broada.carrier.monitor.impl.mw.tomcat;

import com.broada.carrier.monitor.impl.mw.tomcat.jvm.TomcatJVMInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Tomcat状态信息,包括连接器和JVM信息.
 */
public class TomcatStatus implements Tomcat{
  private TomcatJVMInfo tomcatJVMInfo;
  private List<TomcatConnector> connectorList=new ArrayList<TomcatConnector>();
  
  public void setJVMInfo(TomcatJVMInfo _tomcatJVMInfo){
    tomcatJVMInfo=_tomcatJVMInfo;
  }
  
  public TomcatJVMInfo getJVMInfo(){
    return tomcatJVMInfo;
  }
  
  public void addConnector(TomcatConnector tomcatConnector){
    connectorList.add(tomcatConnector);
  }
  
  public List<TomcatConnector> getConnector(){
    return connectorList;
  }
  
}
