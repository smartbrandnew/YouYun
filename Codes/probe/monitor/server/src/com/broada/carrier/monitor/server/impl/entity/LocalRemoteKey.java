package com.broada.carrier.monitor.server.impl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.EntityConst;

@Entity
@Table(name = "mon_map")
public class LocalRemoteKey {
	private int id;
	private String taskId;
	private String localKey;
	private String remoteKey;
	private String remoteType;
	private Date firstTime;

	public LocalRemoteKey() {
	}

	public LocalRemoteKey(String taskId, String localKey, String remoteKey, String remoteType, Date firstTime) {
		this.taskId = taskId;
		this.localKey = localKey;
		this.remoteKey = remoteKey;
		this.remoteType = remoteType;
		this.firstTime = firstTime;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "remote_type", length = EntityConst.ID_LENGTH)
	public String getRemoteType() {
		return remoteType;
	}

	public void setRemoteType(String remoteType) {
		this.remoteType = remoteType;
	}

	@Column(name = "remote_key", length = EntityConst.ID_LENGTH)
	public String getRemoteKey() {
		return remoteKey;
	}

	@Column(name = "first_time")
	public Date getFirstTime() {
		return firstTime;
	}

	public void setRemoteKey(String remoteKey) {
		this.remoteKey = remoteKey;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	@Column(name = "task_id")
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Column(name = "local_key", length = EntityConst.LONG_ID_LENGTH)
	public String getLocalKey() {
		return localKey;
	}

	public void setLocalKey(String localKey) {
		this.localKey = localKey;
	}

	@Override
	public String toString() {
		return String.format("%s[taskId: %s local: %s remote: %s.%s firstTime: %s]", getClass().getSimpleName(),
				getTaskId(), getLocalKey(), getRemoteType(), getRemoteKey(), getFirstTime());
	}
}
