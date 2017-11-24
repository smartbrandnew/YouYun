package com.broada.carrier.monitor.spi.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.broada.carrier.monitor.common.util.SerializeUtil;

@Entity
@Table(name = "mon_temp_data")
public class MonitorTempData {
	private String taskId;
	private Date time;
	private byte[] data;

	public MonitorTempData() {
	}

	public MonitorTempData(String taskId, Date time, byte[] data) {
		this.taskId = taskId;
		this.time = time;
		this.data = data;
	}

	@Id
	@Column(name = "task_id")
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Lob
	@Column(columnDefinition = "blob")
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> cls) {
		if (data == null || data.length == 0)
			return null;

		return (T) SerializeUtil.decodeBytes(data);
	}

	public void setData(Object value) {
		byte[] data;
		if (value instanceof byte[])
			data = (byte[]) value;
		else
			data = SerializeUtil.encodeBytes(value);
		setData(data);
	}
}
