package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

/**
 * 
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-22 13:56:23
 */
public class ExpGroup {
  
  private String id;
  private PerfType type;
  private Producer owner;
  private ExpCollection exps = new ExpCollection();
  
  public ExpCollection getExps() {
    return exps;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Producer getOwner() {
    return owner;
  }
  void setOwner(Producer owner) {
    this.owner = owner;
  }
  public PerfType getType() {
    return type;
  }
  public void setType(PerfType type) {
    this.type = type;
  }
  
  private void readConfig(Element root) throws Exception {
    getExps().clear();
    
    setId(root.getAttribute("id"));   
    String typeStr = XmlUtil.getAttr(root, "type");
    try {
      setType(PerfType.getInstance(typeStr));
    } catch (IllegalArgumentException err) {
      throw new Exception("元素[" + root.getNodeName() + "]属性[type]值不符合规则。", err);
    }
    
    NodeList exps = root.getElementsByTagName("exp");
    for (int i = 0; i < exps.getLength(); i++){      
      getExps().add(Exp.parse((Element)exps.item(i),this));
    }
  }
  
  static ExpGroup parse(Element root,Producer owner) throws Exception {
    ExpGroup group = new ExpGroup();
    group.owner=owner;
    group.readConfig(root);
    return group;
  }
}
