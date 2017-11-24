package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.util.ObjectUtil;
import com.broada.carrier.monitor.common.util.TextUtil;

public class MonitorMethod implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private String name;
	private String typeId;
	private long modified;
	private String descr;
	private DefaultDynamicObject properties = new DefaultDynamicObject();
	private String extra;

	public DefaultDynamicObject getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties.set(properties);
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	public MonitorMethod() {
	}

	public MonitorMethod(String code, String name, String typeId) {
		set(code, name, typeId, null, null, 0, null);
	}

	public MonitorMethod(String code, String name, String typeId, String descr,
			Map<String, Object> props, long modified, String extra) {
		set(code, name, typeId, descr, props, modified, extra);
	}

	public MonitorMethod(MonitorMethod copy) {
		set(copy);
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		if (name == null || name.isEmpty())
			return getCode();
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	@Override
	public String toString() {
		return String.format("%s[code: %s name: %s properties: %s]", getClass()
				.getSimpleName(), getCode(), getName(), getProperties());
	}

	@Override
	public boolean equals(Object obj) {
		MonitorMethod other = (MonitorMethod) obj;
		return this.getCode().equals(other.getCode())
				&& this.getModified() == other.getModified();
	}

	@Override
	public int hashCode() {
		return getCode().hashCode();
	}

	public void verify() {
		if (!TextUtil.isLegalCode(getCode()))
			throw new IllegalArgumentException("编码不允许为空，并且只能由字符、数字、减号与下划线组成");
	}

	public void set(MonitorMethod copy) {
		LinkedHashMap<String, Object> props = new LinkedHashMap<String, Object>();
		props.putAll(copy.getProperties());
		set(copy.getCode(), copy.getName(), copy.getTypeId(), copy.getDescr(),
				props, copy.getModified(), copy.getExtra());
	}

	public void set(String code, String name, String typeId, String descr,
			Map<String, Object> props, long modified, String extra) {
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.typeId = typeId;
		this.modified = modified;
		this.extra = extra;
		this.setProperties(props);
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public boolean equalsData(MonitorMethod item) {
		return ObjectUtil.equals(this.typeId, item.typeId)
				&& ObjectUtil.equals(this.extra, item.extra)
				&& ObjectUtil.equals(this.properties, item.properties);
	}

}
