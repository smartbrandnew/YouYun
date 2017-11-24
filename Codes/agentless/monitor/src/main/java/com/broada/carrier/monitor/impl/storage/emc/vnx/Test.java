package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.util.List;

public class Test {
	public static void main(String[] args) {
//		AbstractEmcCliExecutor capacity = new Capacity();
//		AbsractEmcCliUtils userCapacity = new UserCapacity();
//		List capacityList = capacity.getMonitorValue("cmd.exe /c naviseccli -h 100.2.1.201 getdisk -capacity");
//		List userCapacityList = userCapacity.getMonitorValue("cmd.exe /c naviseccli -h 100.2.1.201 getdisk -usercapacity");
//		for(int i = 0; i < capacityList.size(); i++){
//			String name = capacityList.get(i++).toString();
//			float total = Float.valueOf( (String) capacityList.get(i));
//			float userTotal = Float.valueOf( (String) userCapacityList.get(i));
//			if(total == 0.0 && userTotal == 0.0){
//				System.out.println("+++++++++++++++++++++++++");
//			}else{
//				System.out.println(name + ":" + total + "-" + userTotal);
//				System.out.println(name + ":" + Math.floor(userTotal / total  * 100 *100)/100);
//			}
//		}
		
		/**
		AbstractEmcCliExecutor fan = new LunState();
		List fana = fan.exec("cmd.exe /c naviseccli -h 100.2.1.201 getlun -state");
		System.out.println(fana);
		*/
		capacityTest();
	}
	
	//获取磁盘使用百分比
	public static void capacityTest() {
		AbstractEmcCliExecutor userCapacity = new UserCapacity();
		AbstractEmcCliExecutor Capacity = new Capacity();
		//执行获取磁盘使用命令
		List<Capacity> capacityList = (List<Capacity>)Capacity.exec("cmd.exe /c naviseccli -h 100.2.1.201 getdisk -capacity");
		List<UserCapacity> userCapacityList = (List<UserCapacity>)userCapacity.exec("cmd.exe /c naviseccli -h 100.2.1.201 getdisk -usercapacity");
		int length_1 =  userCapacityList.size();
		int length_2 = capacityList.size();
		System.err.println(" 长度" + length_1 + "    " + length_2);
		System.err.println(" ");
		for (int i=0; i< length_1; i++) {
			Capacity capacityTmp = (Capacity) capacityList.get(i);
			UserCapacity userCapacityTmp = (UserCapacity) userCapacityList.get(i);
			String name = userCapacityTmp.getName();
			String value_1 = userCapacityTmp.getValue();
			String value_2 = capacityTmp.getValue();
			if (!value_2.equals("0")) {
			    System.out.println(name + "  :  "  + value_1 + " || "  + value_2  + " == " +MathUtil.percentages(value_1, value_2));
			}else {
				System.out.println(name + "  :  "  + value_1 + " || "  + value_2  + " == " +0);
			}		
		}
		
	}
}
