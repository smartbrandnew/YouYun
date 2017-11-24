package com.broada.carrier.monitor.probe.impl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;

/**
 * 监测结果缓存
 * @author Jiangjw
 */
@Entity
@Table(name = "mon_result_cache")
public class MonitorResultCache {
	private int id;
	private MonitorResult result;
	
	public MonitorResultCache() {		
	}

	public MonitorResultCache(MonitorResult result) {
		this.result = result;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "task_id", nullable = false)
	public String getTaskId() {
		return result.getTaskId();
	}

	public void setTaskId(String taskId) {
	}

	@Column(nullable = false)
	public Date getTime() {
		return result.getTime();
	}

	public void setTime(Date time) {
	}

	@Lob	
	@Column(nullable = false, columnDefinition = "blob")
	public byte[] getData() {
		return SerializeUtil.encodeBytes(result);
	}

	public void setData(byte[] data) {
		setResult((MonitorResult)SerializeUtil.decodeBytes(data));
	}

	@Transient
	public MonitorResult getResult() {
		return result;
	}

	public void setResult(MonitorResult result) {
		this.result = result;
	}
}
