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
public class UserCapacity extends AbsractEmcCliDisk {
	// 设备名称
	private String name;
	//  设备值
	private String value;

	@Override
	protected List<UserCapacity> resolve(BufferedReader br) throws IOException {
		String str = null;
		List<UserCapacity> resValue = new ArrayList<UserCapacity>();
		StringBuilder sb = new StringBuilder();
		String nameStr = "";
		while ((str = br.readLine()) != null) {
			if (!"".equals(str.trim())) {
				UserCapacity userCapacity = new UserCapacity();
				// 设备值
				if (str.startsWith("Bus")) {
					sb.append(getBusEnclosureName(str));
					sb.append("-硬盘:");
					sb.append(str.substring(str.length() - 2, str.length()).replace(" ", ""));
					nameStr = sb.toString();
					sb.delete(0, sb.length());
				} else {
					String temp = getValue(str);
					if (temp != null) {
						userCapacity.name = nameStr;
						userCapacity.value = temp;
						resValue.add(userCapacity);
					}
				}
			}
		}
		return resValue;
	}

	@Override
	public String getValue(String str) {
		if (str.startsWith("User Capacity:")) {
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
