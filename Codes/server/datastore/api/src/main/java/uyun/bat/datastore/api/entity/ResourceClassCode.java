package uyun.bat.datastore.api.entity;

public enum ResourceClassCode {
	SERVER(0, "Server", "计算机设备"), NETWORK(1, "Router", "网络设备"), VM(2, "VM", "虚拟设备");
	private int id;
	private String classCode;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClassCode() {
		return classCode;
	}

	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private ResourceClassCode(int id, String classCode, String name) {
		this.id = id;
		this.classCode = classCode;
		this.name = name;
	}

	public static ResourceClassCode checkResourceClassCodeByName(String name) {
		for (ResourceClassCode code : ResourceClassCode.values()) {
			if (code.getName().equalsIgnoreCase(name))
				return code;
		}
		return null;
	}

	public static ResourceClassCode checkByClassCode(String classCode) {
		for (ResourceClassCode code : ResourceClassCode.values()) {
			if (code.getClassCode().equals(classCode)) {
				return code;
			}
		}
		return null;
	}

}
