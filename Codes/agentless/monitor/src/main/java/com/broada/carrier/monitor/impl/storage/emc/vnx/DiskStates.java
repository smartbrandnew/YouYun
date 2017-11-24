package com.broada.carrier.monitor.impl.storage.emc.vnx;

public class DiskStates extends AbsractEmcCliDisk {

	@Override
	public String getValue(String str) {
		if(str.startsWith("State")){
			String temp = "";
			if(str.endsWith("Binding")){
				temp = "已绑定逻辑单元";
			}else if(str.endsWith("Empty")){
				temp = "空闲";
			}else if(str.endsWith("Enabled")){
				temp = "启动";
			}else if(str.endsWith("Equalizing")){
				temp = "调均";
			}else if(str.endsWith("Failed")){
				temp = "异常";
			}else if(str.endsWith("Formatting")){
				temp = "格式化";
			}else if(str.endsWith("Off")){
				temp = "掉线";
			}else if(str.endsWith("Powering Up")){
				temp = "加电";
			}else if(str.endsWith("Ready")){
				temp = "就绪";
			}else if(str.endsWith("Rebuilding")){
				temp = "修复";
			}else if(str.endsWith("Removed")){
				temp = "移除";
			}else if(str.endsWith("Hot Spare Ready")){
				temp = "备份磁盘";
			}else if(str.endsWith("Unbound")){
				temp = "未绑定";
			}
			return temp;
		}
		return null;
	}

}
