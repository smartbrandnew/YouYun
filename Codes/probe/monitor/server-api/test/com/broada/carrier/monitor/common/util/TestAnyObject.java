package com.broada.carrier.monitor.common.util;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class TestAnyObject {

	@Test
	public void test() {
		String value = AnyObject.encode(null);
		assertEquals(null, AnyObject.decode(value, String.class));
		
		Object object = "hello";
		value = AnyObject.encode(object);		
		assertEquals(object, AnyObject.decode(value, String.class));
				
		value = object.toString();		
		assertEquals(object, AnyObject.decode(value, String.class));
		
		object = true;
		value = AnyObject.encode(object);		
		assertEquals(object, AnyObject.decode(value, Boolean.class));
		
		object = 12;
		value = AnyObject.encode(object);		
		assertEquals(object, AnyObject.decode(value, Integer.class));
		
		object = 15.7;
		value = AnyObject.encode(object);		
		assertEquals(object, AnyObject.decode(value, Double.class));
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();		
		map.put("1", 1);
		map.put("2", "二");
		object = map;
		value = AnyObject.encode(object);		
		assertEquals(object, AnyObject.decode(value, LinkedHashMap.class));
	}

}
