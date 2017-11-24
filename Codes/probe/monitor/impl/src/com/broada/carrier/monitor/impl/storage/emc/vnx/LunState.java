package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LunState extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		StringBuilder sb = new StringBuilder();
		while ((str = br.readLine()) != null) {
			if(str.startsWith("LOGICAL")){
				resValue.add(str.split(" ")[3]);
			}else if(str.startsWith("State:")){
				resValue.add(getState(str));
			}
		}
		return resValue;
	}
	
	private String getState(String str){

		String temp = "";
		if(str.endsWith("Expanding")){
			temp = "扩大";
		}else if(str.endsWith("Defragmenting")){
			temp = "碎片整理";
		}else if(str.endsWith("Faulted")){
			temp = "损坏";
		}else if(str.endsWith("Transitioning")){
			temp = "迁移";
		}else if(str.endsWith("Bound")){
			temp = "达到边界";
		}else{
			temp = "无法获取";
		}
		return temp;
	}

}
