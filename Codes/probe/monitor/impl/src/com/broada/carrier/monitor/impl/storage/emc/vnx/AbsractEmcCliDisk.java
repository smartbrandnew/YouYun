package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsractEmcCliDisk extends AbstractEmcCliExecutor{

	
	public abstract String getValue(String str);

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		String temp = "";
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null) {
			 if(str.startsWith("Bus")){
				sb.append(getBusEnclosureName(str));
				sb.append("-硬盘:");
				sb.append(str.substring(str.length() -2, str.length()).replace(" ", ""));
				resValue.add(sb.toString());
				sb.delete(0, sb.length());
			 }else{
				 temp = getValue(str);
				 if(temp != null){
					 resValue.add(temp);
				 }
				
			 }
		}
		return resValue;
	}
}
