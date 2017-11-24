package com.broada.carrier.monitor.impl.generic.script;

import java.util.Properties;

import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.numen.agent.script.ScriptExecutor;
import com.broada.numen.agent.script.entity.DynamicParam;

public class ScriptTester {
	public boolean doTest(Properties options) throws ScriptException {
		ScriptResultCollector scriptCollector = new DefaultScriptCollector();
		scriptCollector.init(options);
		return scriptCollector.checkConnectWithAgent();
	}
	
	public DynamicParam[] parseParam(String scriptFile) {
		try {
			return ScriptExecutor.parseParam(System.getProperty("user.dir") + "/" + scriptFile);
		} catch (Throwable e) {
			throw new RuntimeException(ErrorUtil.createMessage("解析脚本参数失败：" + scriptFile, e), e);
		}
	} 
	
	public static boolean test(ServerProbeService service, int probeId, Properties options) {
		return (Boolean) service.executeMethod(probeId, ScriptTester.class.getName(), "doTest", options);			
	}
	
	public static DynamicParam[] parseParam(ServerSystemService service, String scriptFile) {
		DynamicParam[] params = (DynamicParam[]) service.executeMethod(ScriptTester.class.getName(), "parseParam", scriptFile);
		return params;		
	}
}
