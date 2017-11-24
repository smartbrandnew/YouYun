package uyun.bat.web.api.overview.entity;

public class TagKey {
	/**
	 * 总览标签树显示名称
	 */
	private String name = "";
	/**
	 * 总览标签对应的key
	 */
	private String key;

	public TagKey() {
		super();
	}

	public TagKey(String name, String key) {
		super();
		this.name = name;
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
