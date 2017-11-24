package com.broada.carrier.monitor.server.impl.pmdb.map;

import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.component.utils.error.ErrorUtil;

public class MapScript implements MapProcessor {
	private Class<Script> scriptClass;

	public MapScript(String name, String script) {
		this.scriptClass = ScriptUtil.getDefault().compileScript(name, script);
	}

	@Override
	public MapOutput process(MapInput input, LocalRemoteMapper mapper) {
		MapOutput output = new MapOutput();
		Script script;
		try {
			script = scriptClass.newInstance();
		}catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("建立脚本运行实例失败", e);
		}
		script.setProperty("input", input);
		script.setProperty("output", output);
		script.setProperty("util", new MapScriptUtil(input, output, mapper));
		script.run();		
		return output;
	}
}
