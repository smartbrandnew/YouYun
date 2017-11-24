package com.broada.carrier.monitor.impl.host.ipmi.disk;


public enum DiskState {
	
	OK(1, "正常", "OK"), ERROR(2, "异常", "ERROR");
	
	private int id;
	private String name;
	private String code;
	
	private DiskState(int id, String name, String code) {
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
	
	public static DiskState checkById(int id) {
		for (DiskState value : DiskState.values()) {
			if (value.getId() == id)
				return value;
		}
		return null;
	}

	public static DiskState checkByName(String name) {
		for (DiskState value : DiskState.values()) {
			if (value.getName().equals(name))
				return value;
		}
		return null;
	}

	public static DiskState checkByCode(String code) {
		for (DiskState value : DiskState.values()) {
			if (value.getCode().equals(code))
				return value;
		}
		return null;
	}
	
	
}
