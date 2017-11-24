package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.common.util.JsonObject;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "mon_instance")
public class MonitorInstance implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String taskId;
	private String code;
	private String name;
	private JsonObject extra = new JsonObject();

	public MonitorInstance() {
	}

	// todo
	public MonitorInstance(String key) {
		this("", key, key);
	}

	// todo
	public MonitorInstance(String key, String name) {
		this("", key, name);
	}

	@Column(length = EntityConst.DATA_LENGTH)
	public String getExtra() {
		return extra.encode();
	}

	public void setExtra(String extra) {
		this.extra.decode(extra);
	}

	public <T> T retExtra(Class<T> cls) {
		return extra.get(cls);
	}

	public void putExtra(Object extra) {
		this.extra.set(extra);
	}

	public MonitorInstance(String taskId, String code, String name) {
		this.taskId = taskId;
		this.code = code;
		this.name = name;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(length = EntityConst.LONG_ID_LENGTH)
	public String getName() {
		if (name == null || name.isEmpty())
			return getCode();
		return name;
	}

	public void setName(String name) {
		this.name = TextUtil.truncate(name, EntityConst.LONG_ID_LENGTH);
	}

	@Column(length = EntityConst.LONG_ID_LENGTH)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = TextUtil.truncate(code, EntityConst.LONG_ID_LENGTH);
	}

	@Column(name = "task_id")
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		return String.format("%s[taskId: %d code: %s name: %s]", getClass().getSimpleName(), getTaskId(), getCode(),
				getName());
	}

	@JsonIgnore
	public void setInstanceKey(String field) {
		setCode(field);
	}

	@JsonIgnore
	public void setInstanceName(String field) {
		setName(field);
	}
}
