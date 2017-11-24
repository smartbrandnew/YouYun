package com.broada.carrier.monitor.server.api.entity;

import com.broada.carrier.monitor.common.util.TextUtil;

/**
 * 监测节点
 * 
 * @author Jiangjw
 */

public class MonitorNode extends MonitorTarget {
	private static final long serialVersionUID = 1L;
	private Integer probeId;
	private String ip;
	private String tags;
	private String os;
	private String host;

	public MonitorNode() {
	}

	public MonitorNode(String ip) {
		this(null, ip, null, null, 0, ip, 0, "rootDomain");
	}

	public MonitorNode(String id, String name, String typeId, MonitorTargetAuditState auditState, int probeId,
			String ip, long modified, String domainId) {
		set(id, name, typeId, auditState, probeId, ip, modified, domainId);
	}

	public MonitorNode(MonitorNode copy) {
		super(copy);
		this.probeId = copy.probeId;
		this.ip = copy.ip;
		this.tags = copy.tags;
		this.os = copy.os;
		this.host = copy.host;
	}

	public void set(String id, String name, String typeId, MonitorTargetAuditState auditState, int probeId, String ip,
			long modified, String domainId) {
		set(id, name, typeId, auditState, modified, domainId);
		this.probeId = probeId;
		this.ip = ip;
	}

	public void set(MonitorNode copy) {
		super.set(copy);
		this.probeId = copy.probeId;
		this.ip = copy.ip;
	}

	@Override
	public String getId() {
		return super.getId();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * 所属探针
	 * 
	 * @return
	 */

	public Integer getProbeId() {
		return probeId;
	}

	public void setProbeId(Integer probeId) {
		this.probeId = probeId;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals((MonitorNode) obj);
	}

	public String retDisplayName() {
		return String.format("%s[%s]", getName(), getIp());
	}

	public void verify() {
		super.verify();
		if (TextUtil.isEmpty(getIp()))
			throw new IllegalArgumentException("IP地址不能为空");
		if (getProbeId() == null)
			throw new IllegalArgumentException("探针不能为空");
	}

	@Override
	public String toString() {
		return "MonitorNode [probeId=" + probeId + ", ip=" + ip + ", tags="
				+ tags + ", os=" + os + ", host=" + host + "]";
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getTags() {
		return tags;
	}

	public String getOs() {
		return os;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

}
