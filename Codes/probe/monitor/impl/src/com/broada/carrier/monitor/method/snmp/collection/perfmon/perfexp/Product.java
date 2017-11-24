package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

public class Product {
	private String name;	
	private String sysObjectId;
  private Producer owner;
	private PerfCollection perfs = new PerfCollection();
	
  private Product(){
    
  }
  
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSysObjectId() {
		return sysObjectId;
	}
	public void setSysObjectId(String sysObjectId) {
		this.sysObjectId = sysObjectId;
	}
	public PerfCollection getPerfs() {
		return perfs;
	}
	public Producer getOwner() {
    return owner;
  }

  private void readConfig(Element root, Producer producer) throws Exception {
		setName(root.getAttribute("name"));
		setSysObjectId(XmlUtil.getAttr(root, "sysObjectId"));
	  
		NodeList perfs = root.getElementsByTagName("perf");
		for (int i = 0; i < perfs.getLength(); i++){		
			getPerfs().add(Perf.parse((Element)perfs.item(i), producer,this));		
    }
	}
	
	static Product parse(Element root, Producer producer) throws Exception {
		Product product = new Product();
    product.owner=producer;
		product.readConfig(root, producer);
		return product;
	}
	
}
