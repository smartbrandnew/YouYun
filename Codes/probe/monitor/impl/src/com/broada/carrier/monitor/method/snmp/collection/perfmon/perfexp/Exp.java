package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.w3c.dom.Element;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

public class Exp {
	private String id;
	private PerfType type;
	private String value;
	private String instance;
	private String name;
	private ExpGroup owner;
	
	public String getId() {
    return id;
  }

  public String getInstance() {
    return instance;
  }

  public String getName() {
    return name;
  }

  public ExpGroup getOwner() {
    return owner;
  }

  public PerfType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  private void readConfig(Element root) throws Exception {
		id=root.getAttribute("id");
		
		value=XmlUtil.getAttr(root, "value");
		
		if (root.getAttributeNode("instance") == null){
			instance=getValue();
    }else{
			instance=root.getAttribute("instance");
    }
		
		if (root.getAttribute("name") != null){
			name=root.getAttribute("name");
    }
		if (getName() == null || getName().equals("")){
			name=getId();
    }
	}
	
	static Exp parse(Element root,ExpGroup owner) throws Exception {
		Exp exp = new Exp();
    exp.owner=owner;
    exp.type=owner.getType();
		exp.readConfig(root);
		return exp;
	}
}
