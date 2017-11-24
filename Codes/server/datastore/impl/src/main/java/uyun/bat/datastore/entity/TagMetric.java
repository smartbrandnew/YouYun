package uyun.bat.datastore.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TagMetric {
	private Map<String, List<String>> tags = new HashMap<String, List<String>>();
	private String name;

	public TagMetric(String name, String tenantId) {
		this.name = name;
		List<String> list = new ArrayList<String>();
		list.add(tenantId);
		this.tags.put("tenantId", list);
	}

	public Map<String, List<String>> getTags() {
		return tags;
	}

	public void setTags(Map<String, List<String>> tags) {
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TagMetric [tags=" + tags + ", name=" + name + "]";
	}
	
	public TagMetric addTag(String key, String value) {
		if (this.tags.get(key) == null) {
			this.tags.put(key, new ArrayList<String>(Arrays.asList(value)));
		} else {
			this.tags.get(key).add(value);
		}
		return this;
	}

}
