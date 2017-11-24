package com.broada.carrier.monitor.impl.virtual.hypervisor.info;

import java.util.HashMap;
import java.util.Map;



public class CLIHyperVInfoExecutorFactory {
	private static final Map<String, CLIHyperVInfoExecutor> executorCache  = new HashMap<String, CLIHyperVInfoExecutor>();
	public static CLIHyperVInfoExecutor getHyperVExecutor(String srvId){
		CLIHyperVInfoExecutor executor = (CLIHyperVInfoExecutor) executorCache.get(srvId);
		if(executor == null){
			executor = new CLIHyperVInfoExecutor();
			if(!srvId.equals("-1")){
				executorCache.put("" + srvId, executor);
			}
		}
		return executor;
	}
	
	
}
