package uyun.bat.gateway.dd_agent.entity;

/**
 * 网络设备
 */
public class NetEquipment {
	/**
	 * ip
	 */
	private String ip;
	/**
	 * 网络设备类型
	 */
	private String type;
	/**
	 * 产商
	 */
	private String producer;

	/**
	 * 主机名
	 */
	private String host;

	/**
	 * 设备描述
	 */
	private String descr;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

}
