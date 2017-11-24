package com.broada.carrier.monitor.server.api.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.broada.carrier.monitor.common.util.Base64Util;

public class MonitorResultRow extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_INSTCODE = "instCode";
	private static final String PROPERTY_INSTNAME = "instName";
	private static final String PROPERTY_INSTEXTRA = "instExtra";
	private static final String PROPERTY_INSTMONITOR = "instMonitor";
	private static final String PROPERTY_TAGS = "instTags";

	public MonitorResultRow() {
	}

	public MonitorResultRow(String instCode) {
		setInstCode(instCode);
	}

	public MonitorResultRow(String instCode, String instName) {
		setInstCode(instCode);
		setInstName(instName);
	}

	public MonitorResultRow(MonitorInstance inst) {
		this(inst.getCode(), inst.getName());
		setInstExtra(inst.getExtra());
	}

	public boolean isInstMonitor() {
		Boolean value = (Boolean) get(PROPERTY_INSTMONITOR);
		if (value == null)
			return true;
		return value;
	}

	public void setInstMonitor(boolean value) {
		put(PROPERTY_INSTMONITOR, value);
	}

	public void setInstTags(String... tags) {
		if (tags == null)
			return;
		StringBuilder sb = new StringBuilder();
		for (String tag : tags) {
			sb.append(tag);
			sb.append(";");
		}
		int index = sb.lastIndexOf(";");
		if (index == -1)
			return;
		else
			put(PROPERTY_TAGS, sb.substring(0, index));
	}

	public List<String> getTags() {
		List<String> tags = new ArrayList<String>();
		String s = (String) get(PROPERTY_TAGS);
		if (s != null) {
			String[] arr = s.split(";");
			tags.addAll(Arrays.asList(arr));
		}
		return tags;
	}

	public void addTag(String tag) {
		String s = (String) get(PROPERTY_TAGS);
		if (s == null)
			put(PROPERTY_TAGS, tag);
		else {
			s = s + ";" + tag;
			put(PROPERTY_TAGS, s);
		}
	}

	public String getCode() {
		return getInstCode();
	}

	public String getName() {
		return getInstName();
	}

	public String getInstCode() {
		return (String) get(PROPERTY_INSTCODE);
	}

	public void setInstCode(String instCode) {
		put(PROPERTY_INSTCODE, instCode);
	}

	public String getInstExtra() {
		return (String) get(PROPERTY_INSTEXTRA);
	}

	public void setInstExtra(String instExtra) {
		put(PROPERTY_INSTEXTRA, instExtra);
	}

	public String getInstName() {
		String name = (String) get(PROPERTY_INSTNAME);
		if (name == null || name.isEmpty())
			return getInstCode();
		return name;
	}

	public void setInstName(String instName) {
		put(PROPERTY_INSTNAME, instName);
	}

	public static boolean isIndicator(String key) {
		return !(key.equalsIgnoreCase(PROPERTY_INSTCODE) || key.equalsIgnoreCase(PROPERTY_INSTNAME)
				|| key.equalsIgnoreCase(PROPERTY_INSTEXTRA) || key.equalsIgnoreCase(PROPERTY_INSTMONITOR));
	}

	public Object getIndicator(String itemCode) {
		// 加上inst前缀，用来区分key的类型
		Object value = get("inst-" + itemCode);
		return Base64Util.decodeComplex(value);
	}

	public void setIndicator(String itemCode, Object value) {
		// 加上inst前缀，用来区分key的类型
		put("inst-" + itemCode, Base64Util.encodeComplex(value));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : entrySet()) {
			sb.append(" ").append(entry.getKey()).append(": ").append(entry.getValue());
		}
		return sb.toString();
	}

	public MonitorInstance retInstance() {
		MonitorInstance inst = new MonitorInstance(getInstCode(), getInstName());
		inst.setExtra(getInstExtra());
		return inst;
	}
}
