package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import java.util.HashMap;
import java.util.Map;

public class ProductCollection {
	private Map<String, Product> products = new HashMap<String, Product>();

	public Product get(String sysObjectId) {
		return (Product)products.get(getSysObjectId(sysObjectId));
	}

	void clear() {
		products.clear();
	}
	
	private String getSysObjectId(String value) {
		if (value.charAt(0) != '.')
			return "." + value;
		else
			return value;
	}

	public boolean add(Product product) {
		String sysObjectId = getSysObjectId(product.getSysObjectId());
		if (products.containsKey(sysObjectId))
			return false;
		products.put(sysObjectId, product);
		return true;
	}

}
