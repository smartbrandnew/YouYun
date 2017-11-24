package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.common.util.IPUtil;
import com.broada.carrier.monitor.common.util.TextUtil;

/**
 * 监测探针
 * @author Jiangjw
 */
@Entity
@Table(name = "mon_probe")
public class MonitorProbe implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String code;
	private String name;
	private String descr;
	private String host;
	private int port;

	public MonitorProbe() {
	}

	public MonitorProbe(int id, String code, String name, String descr, String host, int port) {
		set(id, code, name, descr, host, port);
	}

	public void set(int id, String code, String name, String descr, String host, int port) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.host = host;
		this.port = port;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(length = 50, nullable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(length = 300)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 500)
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	@Column(length = 100, nullable = false)
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		return getId();
	}

	public String retDisplayName() {
		return String.format("%s[%s]", getName(), getHost());
	}

	@Override
	public boolean equals(Object obj) {
		MonitorProbe other = (MonitorProbe) obj;
		return this.getId() == other.getId()
				&& this.getCode().equals(other.getCode())
				&& this.getHost().equals(other.getHost())
				&& this.getPort() == other.getPort();
	}

	@Override
	public String toString() {
		return String.format("%s[id: %d code: %s addr: %s:%d]", getClass().getSimpleName(),
				getId(), getCode(), getHost(), getPort());
	}

	public void verify() {
		if (TextUtil.isEmpty(getCode()))
			throw new NullPointerException("编码不能为空。");
		if (TextUtil.isEmpty(getName()))
			throw new NullPointerException("名称不能为空。");
		if (TextUtil.isEmpty(getHost()))
			throw new NullPointerException("IP地址不能为空。");
		try {
			IPUtil.parse(getHost());
		} catch (Throwable e) {
			throw new NullPointerException("IP地址不符合规范。");
		}
	}

	public void set(MonitorProbe copy) {
		set(copy.id, copy.code, copy.name, copy.descr, copy.host, copy.port);
	}
}
