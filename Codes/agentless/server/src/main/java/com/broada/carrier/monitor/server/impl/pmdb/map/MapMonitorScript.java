package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.List;

import org.dom4j.Element;

import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.component.utils.error.ErrorUtil;

public class MapMonitorScript extends MapTask {	
	private Class<Script> scriptClass;

	public MapMonitorScript(Element root, String name) {
		super(root, name + "_" + XMLUtil.checkAttribute(root, "name"));		
		String text = root.getText();
		if (text != null)
			text = text.trim();
		if (text == null || text.isEmpty())
			throw new IllegalArgumentException("映射配置script必须配置脚本内容");
		scriptClass = ScriptUtil.getDefault().compileScript(getName(), text);
	}

	public List<MapOutput> process(MapInput input, LocalRemoteMapper mapper) {
		Script script;
		try {
			script = scriptClass.newInstance();
		}catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("建立脚本运行实例失败", e);
		}
		script.setProperty("input", input);		
		MapMonitorContext context = new MapMonitorContext();
		script.setProperty("context", context);
		script.setProperty("util", new MapScriptUtil(input, null , mapper));
		script.run();
		return context.getOutputs();
	}
}
