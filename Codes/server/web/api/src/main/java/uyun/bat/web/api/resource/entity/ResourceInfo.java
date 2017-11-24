package uyun.bat.web.api.resource.entity;

public class ResourceInfo {
	private String name;
	private String attr;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public ResourceInfo(String name) {
		super();
		this.name = name;
	}

	public ResourceInfo() {
		super();
	}

	public ResourceInfo(String name, String attr) {
		super();
		this.name = name;
		this.attr = attr;
	}

}
