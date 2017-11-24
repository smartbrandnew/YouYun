package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.broada.component.utils.error.ErrorUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MapMonitor {
	private String[] types;
	private boolean enabled;
	private List<MapTask> objects = new ArrayList<MapTask>();
	private int index = 0;

	public MapMonitor(Element root) {
		types = XMLUtil.checkAttribute(root, "type").split(",");		
		try {
			enabled = XMLUtil.getAttribute(root, "enabled", true);
						
			for (Object obj : root.elements()) {
				if (obj instanceof Element)
					objects.add(parse((Element)obj));				
			}
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("监测同步映射失败，监测器类型：" + types[0], e);
		}
	}
	
	private MapTask parse(Element element) {
		MapTask obj;
		String name = types[0].replaceAll("-", "_") + "_" + index;
		if (element.getName().equals("object")) {			
			index++;
			obj = new MapObject(element, name);
		} else if (element.getName().equals("script")) {
			obj = new MapMonitorScript(element, name);
		} else
			throw new IllegalArgumentException("未知的element：" + element.getName());
		return obj;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public List<MapTask> getObjects() {
		return objects;
	}

	public String[] getTypes() {
		return types;
	}

	@Override
	public String toString() {
		return String.format("%s[types: %s]", getClass().getSimpleName(), Arrays.toString(types));
	}
}
