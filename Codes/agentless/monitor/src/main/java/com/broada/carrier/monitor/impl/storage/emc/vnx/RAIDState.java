package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RAIDState extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null) {
			if(str.startsWith("RaidGroup ID:")){
				resValue.add(str.substring(15).replaceAll(" ", ""));
			}else if(str.length() > 0){
				String temp = getState(str);
				/*if((str = br.readLine()) != null){
					temp += "和" + getState(str);
				}*/
				resValue.add(temp);
			}
		}
		return resValue;
	}
	
	private String getState(String str){

		String temp = "";
		if(str.endsWith("Invalid")){
			temp = "无效";
		}else if(str.endsWith("Explicit_Remove")){
			temp = "显示移除";
		}else if(str.endsWith("Valid_luns")){
			temp = "有效luns";
		}else if(str.endsWith("Expanding")){
			temp = "扩大";
		}else if(str.endsWith("Defragmenting")){
			temp = "整理碎片";
		}else if(str.endsWith("Halted")){
			temp = "停止";
		}else if(str.endsWith("Busy")){
			temp = "繁忙";
		}else{
			temp = "无法获取";
		}
		return temp;
	}
}
