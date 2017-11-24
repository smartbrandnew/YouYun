package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class MapObject extends MapTask {
	private String id;
	private MapObjectType local;
	private String remote;
	private MapScript script;
	private List<MapItem> items = new ArrayList<MapItem>();

	public MapObject(Element root, String name) {
		super(root, name);
		local = MapObjectType.valueOf(XMLUtil.checkAttribute(root, "local").toUpperCase());
		remote = XMLUtil.checkAttribute(root, "remote");
		id = name + "_" + local.name().toLowerCase() + "2" + remote;
		
		String text = root.getText();
		if (text != null && !text.trim().isEmpty())
			script = new MapScript(id, text);		
		
		Element[] elements = XMLUtil.getElements(root, "item");
		for (Element element : elements) {
			items.add(new MapItem(element));	
		}
	}
	
	public String getId() {
		return id;
	}

	public List<MapItem> getItems() {
		return items;
	}

	public MapScript getScript() {
		return script;
	}

	public MapObjectType getLocal() {
		return local;
	}

	public String getRemote() {
		return remote;
	}

	@Override
	public String toString() {
		return String.format("%s[local: %s remote: %s]", getClass().getSimpleName(), getLocal(), getRemote());
	}
}
