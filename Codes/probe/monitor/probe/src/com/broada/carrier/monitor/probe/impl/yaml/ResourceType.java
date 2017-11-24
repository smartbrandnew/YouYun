package com.broada.carrier.monitor.probe.impl.yaml;




public enum ResourceType {
	SERVER(0, "计算机设备","Server"), NETWORK(1, "网络设备","Network"), VM(2, "虚拟设备","VM"), 
	MINISERVER(5, "小型机", "MiniServer"), DISKARRAY(6, "磁盘阵列","DiskArray"), NAS(7, "网络附属存储","NAS");
	private int id;
	private String name;
	private String code;

	private ResourceType(int id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static ResourceType checkById(int id) {
		for (ResourceType value : ResourceType.values()) {
			if (value.getId() == id)
				return value;
		}
		return ResourceType.SERVER;
	}

	public static ResourceType checkByName(String name) {
		for (ResourceType value : ResourceType.values()) {
			if (value.getName().equals(name))
				return value;
		}
		return ResourceType.SERVER;
	}

	public static ResourceType checkByCode(String code) {
		for (ResourceType value : ResourceType.values()) {
			if (value.getCode().equals(code))
				return value;
		}
		return null;
	}
}
