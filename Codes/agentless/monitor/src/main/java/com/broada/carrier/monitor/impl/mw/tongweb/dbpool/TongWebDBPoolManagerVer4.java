package com.broada.carrier.monitor.impl.mw.tongweb.dbpool;

import java.util.Map;

import javax.management.ObjectName;

import com.broada.carrier.monitor.impl.mw.tongweb.AbstractTongWebManager;

public class TongWebDBPoolManagerVer4 extends AbstractTongWebManager{
	 public TongWebDBPoolManagerVer4(String _ipAddr,String _name,int _port){
	    super.setIpAddr(_ipAddr);
	    super.setName(_name);
	    super.setPort(_port);
	  }

	  public void setMBeanNameInfo(Map map, ObjectName name) {
	    map.put("name", name.getKeyProperty("name"));
	    map.put("type", name.getKeyProperty("type")); 
	  }
}
