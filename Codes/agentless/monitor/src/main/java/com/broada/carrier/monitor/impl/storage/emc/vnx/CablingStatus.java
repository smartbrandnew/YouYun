package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CablingStatus extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		List resValue = new ArrayList();
		br.readLine();
		String str = br.readLine();
		while ((str = br.readLine()) != null) {
		if(str.indexOf("Cabling State")>0){
		if(str.endsWith("Valid")){
			resValue.add("有效");
		}else{
			resValue.add("无效");
		}
		}
		}
		return resValue;
	}

}
