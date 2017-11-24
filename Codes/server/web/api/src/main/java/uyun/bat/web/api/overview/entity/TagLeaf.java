package uyun.bat.web.api.overview.entity;

/**
 * 总览标签树节点
 */
public class TagLeaf {
	/**
	 * 总览标签树显示名称
	 */
	private String name = "";
	/**
	 * 节点对应的标签
	 */
	private String tag;
	private long resource;
	private long warn;
	private long error;

	public TagLeaf() {
		super();
	}

	public TagLeaf(String name, String tag, long resource, long warn, long error) {
		super();
		this.name = name;
		this.tag = tag;
		this.resource = resource;
		this.warn = warn;
		this.error = error;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getResource() {
		return resource;
	}

	public void setResource(long resource) {
		this.resource = resource;
	}

	public long getWarn() {
		return warn;
	}

	public void setWarn(long warn) {
		this.warn = warn;
	}

	public long getError() {
		return error;
	}

	public void setError(long error) {
		this.error = error;
	}

}
