package com.broada.carrier.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class TypeUtil {
	public static Map<String, String> loadTypesFromPackage() {
		Map<String, String> map=new HashMap<String, String>();
		ServiceLoader<MonitorPackage> loader = ServiceLoader.load(MonitorPackage.class);
		try {
			for (MonitorPackage pack : loader) {
				if(pack.getTypes()!=null){
				for (MonitorType type : pack.getTypes()) {
					String groupId=type.getGroupId().toUpperCase();
					String id=type.getId().toUpperCase();
					map.put(id, groupId);
				}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static void main(String[] args){
		System.out.println(loadTypesFromPackage());
	}
}
