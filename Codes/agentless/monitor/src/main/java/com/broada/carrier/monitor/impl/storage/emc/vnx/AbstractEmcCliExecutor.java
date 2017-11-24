package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public abstract class AbstractEmcCliExecutor {
	public  List exec(String command){
		try {
			java.lang.Process p = Runtime.getRuntime().exec(command);
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			List resValue = resolve(br);
			br.close();
			is.close();
			return resValue;
		} catch (Exception e) {
			throw new RuntimeException("执行设备命令出错",e);
		}
	}
	
	protected abstract List resolve(BufferedReader br) throws IOException;
	
	protected String getBusEnclosureName(String str){
		StringBuilder sb = new StringBuilder();
		sb.append("后端链路:");
		sb.append(str.substring(4,6).replace(" ", ""));
		sb.append("-盘柜:");
		sb.append(str.substring(16,18).replace(" ", ""));
		return sb.toString();
	}
}
