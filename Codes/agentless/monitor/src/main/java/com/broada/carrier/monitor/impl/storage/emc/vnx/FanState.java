package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FanState extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null) {
			if(str.startsWith("Bus")){
				//这里暂时没有去获取细粒度的状态
				//可以获取的每个disk的状态״̬
//				resValue.add(this.getBusEnclosureName(str));
				resValue.add(getState(str));
			}else if(str.startsWith("Enclosure")){
				resValue.add(getState(str));
			}
		}
		return resValue;
	}
	
	private String getState(String str){

		String temp = "";
		if(str.endsWith("Present")){
			temp = "运行正常";
		}else if(str.endsWith("Empty")){
			temp = "异常";
		}else if(str.endsWith("Not Connected")){
			temp = "不能连接";
		}else if(str.endsWith("Removed")){
			temp = "移除";
		}else{
			temp = "无法获取";
		}
		return temp;
	}

}
