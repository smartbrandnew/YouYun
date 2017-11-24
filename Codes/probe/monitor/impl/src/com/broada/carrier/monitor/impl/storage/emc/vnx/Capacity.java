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
public class Capacity extends AbsractEmcCliDisk {
	// 设备名称
	private String name;
	//设备值
	private String value;

	@Override
	protected List<Capacity> resolve(BufferedReader br) throws IOException {
		String str = null;
		List<Capacity> resValue = new ArrayList<Capacity>();
		StringBuilder sb = new StringBuilder();
		String nameStr = "";
		while ((str = br.readLine()) != null) {
			if (!"".equals(str.trim())) {
				Capacity capacity = new Capacity();
				//截取字符,获取磁盘使用值
				if (str.startsWith("Bus")) {
					sb.append(getBusEnclosureName(str));
					sb.append("-硬盘:");
					sb.append(str.substring(str.length() - 2, str.length()).replace(" ", ""));
					nameStr = sb.toString();
					sb.delete(0, sb.length());
				} else {
					String temp = getValue(str);
					if (temp != null) {
						capacity.name = nameStr;
						capacity.value = temp;
						resValue.add(capacity);
					}
				}
			}
		}
		return resValue;
	}

	@Override
	public String getValue(String str) {
		if (str.startsWith("Capacity:")) {
			return str.substring(14).replace(" ", "");
		}
		return "0";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
