package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;

public class MapConfig {
	private static final Logger logger = LoggerFactory.getLogger(MapConfig.class);
	private Map<String, MapMonitor> monitors = new HashMap<String, MapMonitor>();
	private Map<String, MapFunction> functions = new HashMap<String, MapFunction>();

	public MapConfig() {
		this(Config.getConfDir() + "/pmdb-map.xml");
	}

	public MapConfig(String filename) {
		this(new File(filename));
	}

	public MapConfig(File file) {
		try {
			if (!file.exists())
				logger.warn("映射配置文件不存在：" + file);
			else
				parse(file, XMLUtil.parseXmlFile(file));
		} catch (DocumentException e) {
			throw ErrorUtil.createRuntimeException("映射配置文件解析失败：" + file, e);
		}
	}

	private void parse(File file, Document doc) {
		Element root = doc.getRootElement();
		if (root == null || !root.getName().equals("pmdb-map"))
			throw new IllegalArgumentException("XML解析错误，根元素必须是pmdb-map");

		Element[] elements = XMLUtil.getElements(root, "include");
		for (Element element : elements) {
			String filename = XMLUtil.checkAttribute(element, "file");
			MapConfig includeConfig = new MapConfig(new File(file.getParentFile(), filename));
			HashSet<MapMonitor> added = new HashSet<MapMonitor>();
			for (MapMonitor monitor : includeConfig.monitors.values()) {
				if (added.contains(monitor))
					continue;
				addMonitor(monitor);
				added.add(monitor);
			}
			this.monitors.putAll(includeConfig.monitors);
			this.functions.putAll(includeConfig.functions);
		}

		elements = XMLUtil.getElements(root, "monitor");
		for (Element element : elements) {
			addMonitor(new MapMonitor(element));
		}

		elements = XMLUtil.getElements(root, "function");
		for (Element element : elements) {
			addFunction(new MapFunction(element));
		}
	}

	private void addFunction(MapFunction function) {
		MapFunction exists = getFunction(function.getName());
		if (exists != null) {
			logger.warn("映射函数已经存在，将忽略当前版本：{}", function);
			return;
		}
		functions.put(function.getName(), function);
	}

	public MapFunction getFunction(String name) {
		return functions.get(name);
	}

	private void addMonitor(MapMonitor monitor) {
		if (!monitor.isEnabled())
			return;

		for (String type : monitor.getTypes()) {
			MapMonitor exists = getMonitor(type);
			if (exists != null) {
				logger.warn("映射配置已经存在，将忽略当前版本：{}", monitor);
				return;
			}
			logger.debug("映射配置添加：{} = {}", type, monitor);
			monitors.put(type, monitor);
		}
	}

	public MapMonitor getMonitor(String type) {
		return monitors.get(type);
	}
}
