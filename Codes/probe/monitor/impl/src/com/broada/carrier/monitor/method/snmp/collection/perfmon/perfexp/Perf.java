package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.w3c.dom.Element;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

public class Perf {
  private PerfType type;
  private ExpGroup group;
  private Exp exp;
  private Product owner;

  private Perf() {
  }

  public Exp getExp() {
    return exp;
  }

  public PerfType getType() {
    return type;
  }

  public ExpGroup getGroup() {
    return group;
  }

  public Product getOwner() {
    return owner;
  }

  static Perf parse(Element root, Producer producer, Product product) throws Exception {
    Perf perf = new Perf();
    perf.owner = product;
    perf.readConfig(root, producer);
    return perf;
  }

  private void readConfig(Element root, Producer producer) throws Exception {
    String typeStr = XmlUtil.getAttr(root, "type");
    try {
      this.type = PerfType.getInstance(typeStr);
    } catch (IllegalArgumentException err) {
      throw new Exception("元素[" + root.getNodeName() + "]属性[type]值不符合规则。", err);
    }
    String groupStr = XmlUtil.getAttr(root, "expgroup");
    group = producer.getExpGroups().get(groupStr);
    if (group == null) {
      throw new Exception("元素[" + root.getNodeName() + "]属性[expgroup]值中的表达式ID，未在此产商表达式集合中配置。");
    }
    String expStr = XmlUtil.getAttr(root, "exp");
    exp = group.getExps().get(expStr);
    if (exp == null) {
      throw new Exception("元素[" + root.getNodeName() + "]属性[exp]值中的表达式ID，未在此产商表达式集合中配置。");
    }
  }
}
