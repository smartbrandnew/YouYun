package com.broada.carrier.monitor.server.impl.pmdb.map;

import org.dom4j.Element;

import com.broada.component.utils.error.ErrorUtil;

public class MapFunction {
	private String name;
	private Class<Script> scriptClass;

	public MapFunction(Element root) {
		name = XMLUtil.checkAttribute(root, "name");
		String text = root.getText();
		if (text != null)
			text = text.trim();
		if (text == null || text.isEmpty())
			throw new IllegalArgumentException("映射配置function必须配置脚本内容");
		scriptClass = ScriptUtil.getDefault().compileScript(name, text);
	}

	public String getName() {
		return name;
	}
	
	public Object run(Object input) {
		Script script;
		try {
			script = scriptClass.newInstance();
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("建立脚本实例失败", e);
		} 
		script.setProperty("input", input);
		return script.run();		
	}
}
