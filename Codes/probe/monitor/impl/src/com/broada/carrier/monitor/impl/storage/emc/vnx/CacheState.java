package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Administrator
 *
 */
public class CacheState extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		String temp = "";
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null) {
			 if(str.startsWith("SP Read")){
				 resValue.add("sp读取缓存的状态"); 
				 resValue.add(getValue(str));
			 }else if(str.startsWith("SP Write")){
				 resValue.add("sp写缓存的状态"); 
				 resValue.add(getValue(str));
			 }
		}
		return resValue;
	}
	
	private String getValue(String str){
		if(str.endsWith("Enabled")){
			return "已启动";
		}
		return "禁用";
	}
}
