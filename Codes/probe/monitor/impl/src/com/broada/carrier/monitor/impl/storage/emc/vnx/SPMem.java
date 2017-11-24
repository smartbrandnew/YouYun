package com.broada.carrier.monitor.impl.storage.emc.vnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SPMem extends AbstractEmcCliExecutor{

	@Override
	protected List resolve(BufferedReader br) throws IOException {
		String str = null;
		List resValue = new ArrayList();
		while ((str = br.readLine()) != null) {
			if(str.startsWith("SP")){
				resValue.add(str);
			}else if(str.length() > 0){
				resValue.add(str.split(":")[1].replaceAll(" ", ""));
			}
		}
		return resValue;
	}

}
