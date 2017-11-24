package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

public class Producer {
	private String name;
	private long code;
	private ExpGroupCollection groups = new ExpGroupCollection();
	private ProductCollection products = new ProductCollection();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCode() {
		return code;
	}
	public void setCode(long code) {
		this.code = code;
	}
	public ExpGroupCollection getExpGroups() {
		return groups;
	}
	public ProductCollection getProducts() {
		return products;
	}
	
	private void readConfig(Element root) throws Exception {
    getExpGroups().clear();
		getProducts().clear();
		
		setName(root.getAttribute("name"));		
		
		try {
			setCode(Long.parseLong(XmlUtil.getAttr(root, "code"))); 
		} catch (NumberFormatException err) {
			throw new Exception("元素[" + root.getNodeName() + "]属性[code]必须是数字数据。", err);
		}
		//加载expgroup
		NodeList exps = root.getElementsByTagName("expgroup");
		for (int i = 0; i < exps.getLength(); i++){	
      getExpGroups().add(ExpGroup.parse((Element)exps.item(i),this));
    }
    //加载product节点
		NodeList products = root.getElementsByTagName("product");
		for (int i = 0; i < products.getLength(); i++){			
			getProducts().add(Product.parse((Element)products.item(i), this));
    }
	}
	
	static Producer parse(Element root) throws Exception {
		Producer producer = new Producer();
		producer.readConfig(root);
		return producer;
	}
}
